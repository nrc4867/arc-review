package dev.chieppa.arc.ui.actions

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.core.start.StartArgs
import dev.chieppa.arc.core.start.StartArrange
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.start.SideBySide
import dev.chieppa.arc.ui.start.StartReview
import dev.chieppa.arc.ui.workbench.ActionTargets
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.ActionRegistry

/**
 * Registers the actions the start screen's rows, buttons, links and menus run. Those that need what a click names, such
 * as a link or a card, take it from the start screen's arguments (see [ActionTargets.start]) and are not offered from
 * the palette; those that act on a source act on the one named, else the active one.
 */
fun registerStartActions(registry: ActionRegistry, workbench: Workbench) {
    fun action(id: ActionId, available: () -> Boolean, run: () -> Unit) =
        registry.register(Action(id, id.value, id.value, emptyList(), isAvailable = available, run = run))

    val state = workbench.state
    val start = workbench.start
    fun arg(name: String): String? = ActionTargets.start(workbench)?.get(name)
    fun source(): Source? = ActionTargets.source(workbench)
    fun card(): String? = arg(StartArgs.CARD)?.takeIf { start.showing.columnOf(it) != null }

    action(ArcActionIds.OPEN_SOURCE, { arg(StartArgs.SOURCE)?.let(SourceRefs::parse) != null }) {
        arg(StartArgs.SOURCE)?.let(SourceRefs::parse)?.let(workbench::openSource)
    }
    action(ArcActionIds.OPEN_LINK, { arg(StartArgs.URL) != null }) { arg(StartArgs.URL)?.let(workbench.links::open) }
    action(ArcActionIds.OPEN_FILTER, { arg(StartArgs.FILTER)?.let(workbench.watch.watchlist::filter) != null }) {
        arg(StartArgs.FILTER)?.let(workbench.panes::openFilter)
    }
    action(ArcActionIds.CONTINUE_REVIEW, { source()?.let { StartReview.nextFile(state, it) } != null }) {
        val source = source() ?: return@action
        StartReview.nextFile(state, source)?.let { workbench.panes.openChange(source, it) }
    }
    action(ArcActionIds.TOGGLE_SINCE_REVIEW, { state.active != null }) { workbench.sinceReview = !workbench.sinceReview }
    action(ArcActionIds.OPEN_CONVERSATION, { source() is Source.PullRequest }) {
        val source = source() ?: return@action
        if (source != state.active) workbench.activateSource(source)
        workbench.panes.openConversation(source)
    }
    action(ArcActionIds.OPEN_SIDE_BY_SIDE, { SideBySide.openable(workbench, state.selection).size > 1 }) {
        SideBySide.open(workbench, state.selection)
    }
    action(ArcActionIds.REVIEW_STAGED, { state.active?.let(workbench.comments.pusher::canPush) == true }) {
        if (workbench.frameLayout[DrawerIds.STAGED_COMMENTS]?.open != true) workbench.toggleDrawer(DrawerIds.STAGED_COMMENTS)
        workbench.comments.pusher.confirming = state.active
    }
    action(ArcActionIds.CLEAR_RECENT, { start.recent.sources.isNotEmpty() }, start::clearRecent)
    action(ArcActionIds.FORGET_RECENT, { arg(StartArgs.SOURCE)?.let { ref -> start.recent.sources.any { it.ref == ref } } == true }) {
        arg(StartArgs.SOURCE)?.let(start::forget)
    }
    action(ArcActionIds.HIDE_START_CARD, { card() != null }) { card()?.let { id -> start.arrange { StartArrange.hide(it, id) } } }
    fun move(id: ActionId, step: Int) = action(id, { card()?.let { StartArrange.canMove(start.showing, it, step) } == true }) {
        card()?.let { card -> start.arrange { StartArrange.move(it, start.showing, card, step) } }
    }
    move(ArcActionIds.MOVE_START_CARD_UP, -1)
    move(ArcActionIds.MOVE_START_CARD_DOWN, 1)
    action(ArcActionIds.MOVE_START_CARD_ACROSS, { card() != null }) {
        card()?.let { card -> start.arrange { StartArrange.moveAcross(it, start.showing, card) } }
    }
    action(ArcActionIds.RESTORE_START_CARDS, { !start.screen.isDefault }) { start.arrange { it.copy(hidden = emptySet(), shown = emptySet(), moves = emptyMap()) } }
}
