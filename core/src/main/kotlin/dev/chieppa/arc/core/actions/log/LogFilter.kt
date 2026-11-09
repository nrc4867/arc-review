package dev.chieppa.arc.core.actions.log

import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.RunStep

/**
 * Reads a finished job's log one line at a time and keeps only what is worth keeping, holding no more than a step's
 * tail at once: the last [LogLimits.tail] lines of each step ([LogLimits.failedTail] of a failed step), every error and
 * warning line with the lines before it, and the longest silences with the lines either side. A line belongs to the
 * last of [steps] that had started by its time.
 */
class LogFilter(steps: List<RunStep>, private val limits: LogLimits = LogLimits()) {
    private val starts = steps.filter { it.startedAt != null }.sortedBy { it.number }.map { it.number to it.startedAt!!.epochSecond }
    private val failed = steps.filter { it.state == RunState.Failed }.map { it.number }.toSet()
    private val kept = sortedMapOf<Int, KeptLine>()
    private val recent = ArrayDeque<KeptLine>()
    private val tail = ArrayDeque<KeptLine>()
    private val silences = mutableListOf<Silence>()
    private var step = starts.firstOrNull()?.first ?: 0
    private var index = 0
    private var bytes = 0L
    private var lastAt: Long? = null

    private class Silence(val afterIndex: Int, val seconds: Long, val lines: MutableList<KeptLine>, var need: Int)

    /** Takes the next line of the log. */
    fun accept(raw: String) {
        bytes += raw.toByteArray(Charsets.UTF_8).size + 1L
        val (at, text) = LogLineText.split(raw)
        if (at != null) stepAt(at)
        val line = KeptLine(index++, step, at ?: lastAt, text)
        for (silence in silences) if (silence.need > 0) {
            silence.lines += line
            silence.need--
        }
        val previous = lastAt
        if (at != null && previous != null) silence(line, at - previous)
        if (at != null) lastAt = at
        if (LogLineText.isProblem(text)) {
            recent.forEach(::keep)
            keep(line)
        }
        tail.addLast(line)
        while (tail.size > tailLimit(step)) tail.removeFirst()
        recent.addLast(line)
        while (recent.size > limits.beforeProblem) recent.removeFirst()
    }

    /** What was kept, once the whole log has been read. */
    fun finish(): KeptLog {
        tail.forEach(::keep)
        tail.clear()
        silences.forEach { it.lines.forEach(::keep) }
        val found = silences.sortedBy { it.afterIndex }.map { LogSilence(it.afterIndex, it.seconds) }
        return KeptLog(kept.values.toList(), index, bytes, found)
    }

    private fun stepAt(at: Long) {
        val next = starts.lastOrNull { it.second <= at }?.first ?: return
        if (next == step) return
        tail.forEach(::keep)
        tail.clear()
        step = next
    }

    private fun silence(line: KeptLine, seconds: Long) {
        if (seconds <= 0 || limits.silences == 0) return
        val smallest = silences.minByOrNull { it.seconds }
        if (silences.size >= limits.silences && smallest != null && smallest.seconds >= seconds) return
        if (silences.size >= limits.silences) silences.remove(smallest)
        val before = recent.toList().takeLast(limits.aroundSilence)
        silences += Silence(line.index, seconds, (before + line).toMutableList(), limits.aroundSilence - 1)
    }

    private fun tailLimit(step: Int): Int = if (step in failed) limits.failedTail else limits.tail

    private fun keep(line: KeptLine) {
        kept[line.index] = line
    }
}
