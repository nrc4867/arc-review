package dev.chieppa.arc.syntax.facts

/**
 * Hands each member (a parameter or an argument, by its start) to the innermost list (a parameter or argument list,
 * by its range) that holds it, so the parameters of a lambda inside a default value, or the arguments of a call
 * nested in another call's arguments, are not counted twice.
 */
internal class ListMembers {
    private val lists = ArrayList<IntRange>()
    private val members = ArrayList<Pair<Int, String>>()

    fun addList(start: Int, end: Int) {
        if (end > start) lists += start until end
    }

    fun addMember(start: Int, text: String) {
        members += start to text
    }

    /** Each list's members in order, by the list's start; a list with none maps to an empty list. */
    fun byList(): Map<Int, List<String>> {
        // Lists are syntax nodes, so they nest properly: a sweep keeps the open ones on a stack, innermost on top.
        val sorted = lists.distinct().sortedWith(compareBy<IntRange> { it.first }.thenByDescending { it.last })
        val owned = sorted.associate { it.first to ArrayList<String>() }
        val open = ArrayDeque<IntRange>()
        var next = 0
        for ((start, text) in members.sortedBy { it.first }) {
            while (next < sorted.size && sorted[next].first <= start) {
                while (open.isNotEmpty() && open.last().last < sorted[next].first) open.removeLast()
                open.addLast(sorted[next++])
            }
            while (open.isNotEmpty() && open.last().last < start) open.removeLast()
            open.lastOrNull()?.let { owned.getValue(it.first) += text }
        }
        return owned
    }
}
