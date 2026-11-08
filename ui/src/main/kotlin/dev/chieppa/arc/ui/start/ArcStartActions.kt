package dev.chieppa.arc.ui.start

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.start.StartAction
import dev.chieppa.arc.core.start.StartArgs
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.stretch.core.ActionId

/** The start actions Arc's own cards run, with their arguments. */
object ArcStartActions {
    fun of(id: ActionId): StartAction = StartAction(id.value)

    fun on(id: ActionId, source: Source): StartAction = StartAction(id.value, mapOf(StartArgs.SOURCE to source.ref))

    fun openChange(source: Source, path: String): StartAction =
        StartAction(ArcActionIds.OPEN_CHANGE.value, mapOf(StartArgs.SOURCE to source.ref, StartArgs.PATH to path))

    fun openSource(ref: String): StartAction = StartAction(ArcActionIds.OPEN_SOURCE.value, mapOf(StartArgs.SOURCE to ref))

    fun forgetRecent(ref: String): StartAction = StartAction(ArcActionIds.FORGET_RECENT.value, mapOf(StartArgs.SOURCE to ref))

    fun openLink(url: String): StartAction = StartAction(ArcActionIds.OPEN_LINK.value, mapOf(StartArgs.URL to url))

    fun openFilter(filter: String): StartAction = StartAction(ArcActionIds.OPEN_FILTER.value, mapOf(StartArgs.FILTER to filter))

    fun onCard(id: ActionId, card: String): StartAction = StartAction(id.value, mapOf(StartArgs.CARD to card))
}
