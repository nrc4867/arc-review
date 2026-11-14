package dev.chieppa.arc.core.activity

/**
 * The operations Arc ran and what plugins wrote, newest last, keeping the last [capacity] of each channel, so a busy
 * channel never pushes out another's. Every entry's texts pass [ActivityRedaction] first, so no token is kept. Safe to
 * report into from any thread; listeners hear each entry on the reporting thread.
 */
class ActivityLog(private val capacity: Int = DEFAULT_CAPACITY) : ActivityReporter {
    private val channels = LinkedHashMap<String, ArrayDeque<ActivityEntry>>()
    private val titles = LinkedHashMap<String, ActivityChannel>()
    private val listeners = mutableListOf<(ActivityEntry) -> Unit>()
    private var next = 1L

    init {
        require(capacity > 0) { "capacity must be positive" }
    }

    override fun report(entry: ActivityEntry) {
        val kept: ActivityEntry
        val hearing: List<(ActivityEntry) -> Unit>
        synchronized(this) {
            kept = ActivityRedaction.entry(entry).copy(id = next++)
            val channel = channels.getOrPut(kept.channel.id) { ArrayDeque() }
            titles[kept.channel.id] = kept.channel
            channel.addLast(kept)
            while (channel.size > capacity) channel.removeFirst()
            hearing = listeners.toList()
        }
        hearing.forEach { it(kept) }
    }

    /** The entries kept in [channel], or in every channel when null, oldest first. */
    fun entries(channel: String? = null): List<ActivityEntry> = since(0, channel)

    /** The entries kept in [channel], or in every channel when null, after the one with [id], oldest first. */
    @Synchronized
    fun since(id: Long, channel: String? = null): List<ActivityEntry> {
        val queues = if (channel == null) channels.values else listOfNotNull(channels[channel])
        return queues.flatMap { queue -> tail(queue, id) }.sortedBy { it.id }
    }

    /** The channels that have had an entry, as last reported, in the order they first did. */
    @Synchronized
    fun channels(): List<ActivityChannel> = titles.values.toList()

    /** Empties [channel], or every channel when null; later entries keep counting on from the last id. */
    @Synchronized
    fun clear(channel: String? = null) {
        if (channel == null) channels.values.forEach { it.clear() } else channels[channel]?.clear()
    }

    /** Calls [listener] with each entry reported from now on, until the result is closed. */
    fun listen(listener: (ActivityEntry) -> Unit): AutoCloseable {
        synchronized(this) { listeners += listener }
        return AutoCloseable { synchronized(this) { listeners -= listener } }
    }

    private fun tail(queue: ArrayDeque<ActivityEntry>, id: Long): List<ActivityEntry> {
        var first = queue.size
        while (first > 0 && queue[first - 1].id > id) first--
        return queue.subList(first, queue.size).toList()
    }

    companion object {
        const val DEFAULT_CAPACITY = 5000
    }
}
