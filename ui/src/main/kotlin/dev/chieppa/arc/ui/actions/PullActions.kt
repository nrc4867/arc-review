package dev.chieppa.arc.ui.actions

import dev.chieppa.arc.ui.workbench.ActionTargets
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.ActionRegistry

/**
 * Registers the actions on a pull request's row in a saved filter's list. Each acts on the row the menu is open on,
 * so none is offered from the palette.
 */
fun registerPullActions(registry: ActionRegistry, workbench: Workbench) {
    fun action(id: ActionId, available: (MenuTarget.OfPull) -> Boolean, run: (MenuTarget.OfPull) -> Unit) =
        registry.register(
            Action(
                id,
                id.value,
                id.value,
                emptyList(),
                isAvailable = { ActionTargets.pull(workbench)?.let(available) == true },
                run = { ActionTargets.pull(workbench)?.let(run) },
            ),
        )

    val state = workbench.state
    action(ArcActionIds.OPEN_PULL, { it.source != null }) { target -> target.source?.let(workbench::openSource) }
    action(ArcActionIds.ADD_PULL, { it.source != null && it.source !in state.review.sources }) { target ->
        target.source?.let { workbench.addSourceBeside(it.ref) }
    }
    action(ArcActionIds.OPEN_PULL_IN_NEW_REVIEW, { it.source != null }) { target -> target.source?.let { workbench.onNewReviewWith(it.ref) } }
    action(ArcActionIds.OPEN_PULL_REVIEW, { target -> target.source?.let(workbench::otherReviewHolding) != null }) { target ->
        target.source?.let(workbench::otherReviewHolding)?.let { workbench.onOpenReview(it.id) }
    }
    action(ArcActionIds.OPEN_PULL_ON_GITHUB, { true }) { workbench.links.open(it.pull.url) }
    action(ArcActionIds.COPY_PULL_LINK, { true }) { workbench.clipboard.copy(it.pull.url) }
}
