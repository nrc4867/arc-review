package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.MyPull
import dev.chieppa.arc.ui.pulls.FilterResult

/**
 * The pull request searches the Mine rules need beside the saved filters: the person's open pull requests and those
 * assigned to them, polled in the same request, under keys no filter has.
 */
object RunSearches {
    const val AUTHORED = "runs:authored"
    const val ASSIGNED = "runs:assigned"

    val ALL: Map<String, String> = mapOf(AUTHORED to "is:open is:pr author:@me", ASSIGNED to "is:open is:pr assignee:@me")

    /** The person's open pull requests from what those searches found, a written one over an assigned one. */
    fun pulls(results: Map<String, FilterResult>): List<MyPull> {
        val authored = results[AUTHORED]?.pulls.orEmpty().map { MyPull(it.repo.fullName, it.number, it.headOid, authored = true) }
        val keys = authored.map { it.repo.lowercase() to it.number }.toSet()
        val assigned = results[ASSIGNED]?.pulls.orEmpty().filter { (it.repo.fullName.lowercase() to it.number) !in keys }
            .map { MyPull(it.repo.fullName, it.number, it.headOid, authored = false) }
        return authored + assigned
    }
}
