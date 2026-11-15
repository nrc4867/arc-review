package dev.chieppa.arc.ui.actions

import dev.chieppa.arc.ui.workbench.ActionTargets
import dev.chieppa.arc.ui.workbench.GitHubTargets
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.KeyChord

/**
 * Registers the actions that open what the window shows on github.com in the browser: the target source's page, and the
 * target file in its pull request's Files view or at its revision, at the line a pane shows at its top. Each acts on the
 * open menu's target, or from the palette and its key on the active source and the focused file (see [ActionTargets]).
 */
fun registerHostLinkActions(registry: ActionRegistry, workbench: Workbench) {
    fun action(id: ActionId, keys: List<KeyChord>, url: () -> String?) =
        registry.register(Action(id, id.value, id.value, keys, isAvailable = { url() != null }, run = { url()?.let(workbench.links::open) }))

    action(ArcActionIds.OPEN_ON_GITHUB, listOf(ArcKeys.OPEN_ON_GITHUB)) {
        ActionTargets.source(workbench)?.let { GitHubTargets.source(workbench, it) }
    }
    action(ArcActionIds.OPEN_FILE_ON_GITHUB, emptyList()) {
        val pane = ActionTargets.pane(workbench)
        val file = ActionTargets.file(workbench)
        if (file != null) {
            val (source, path) = file
            val line = pane?.takeIf { it.source == source && it.path == path && it.hasChange }?.let { GitHubTargets.lineOf(workbench, it) }
            GitHubTargets.file(workbench, source, path, line)
        } else {
            pane?.let { GitHubTargets.pane(workbench, it, GitHubTargets.lineOf(workbench, it)) }
        }
    }
}
