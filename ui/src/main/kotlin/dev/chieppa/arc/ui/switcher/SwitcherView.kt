package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.KeyEvent
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.chord
import java.nio.file.Path

/**
 * What a switcher field's text comes to: a pasted [target] with the ways it can [options] open and its [facts], a pasted
 * [folder] to follow as a repo, or else a search in [sections]. The palette and the start screen's field both show it.
 */
class SwitcherView(
    val target: PastedTarget?,
    val folder: Path?,
    val folderFailed: Boolean,
    val sections: List<SwitcherSection>,
    val options: List<OpenAs>,
    val facts: LinkFacts,
    private val onFolderFailed: () -> Unit,
) {
    val searching: Boolean get() = target == null && folder == null
    val rows: List<SwitcherItem> get() = sections.flatMap { it.items }

    /** How many rows Up and Down move through. */
    val count: Int
        get() = when {
            folder != null -> 1
            target == null -> rows.size
            else -> options.size
        }

    /** Follows the repo in [folder] through [commands]. */
    fun addFolder(commands: SwitcherCommands) {
        folder?.let { commands.addRepo(it, onFolderFailed) }
    }

    /**
     * What a key in the field does to [state]: Enter and its variants pick the highlighted row through [commands], Up and
     * Down move, and with [scopes] Tab changes the scope. Whether it did anything.
     */
    fun onKey(event: KeyEvent, state: SwitcherState, commands: SwitcherCommands, scopes: Boolean = true): Boolean {
        val chord = event.chord()
        val pick = SwitcherKeys.pickOf(event)
        return when {
            pick != null && folder != null -> { addFolder(commands); true }
            pick != null && target == null -> rows.getOrNull(state.index)?.let { commands.pick(it, pick) } != null
            pick != null -> options.getOrNull(state.index)?.let { commands.pick(it, pick) } != null
            chord == SwitcherKeys.DOWN -> { state.move(1, count); true }
            chord == SwitcherKeys.UP -> { state.move(-1, count); true }
            scopes && chord == SwitcherKeys.SCOPE && searching -> { state.pick(state.scope.next()); true }
            scopes && chord == SwitcherKeys.SCOPE_BACK && searching -> { state.pick(state.scope.next(back = true)); true }
            else -> false
        }
    }
}

/** What [state]'s field comes to on [workbench], kept current as it is typed in. */
@Composable
fun rememberSwitcherView(workbench: Workbench, state: SwitcherState): SwitcherView {
    val query = state.query
    val target = remember(query, workbench.gitHubRepos, workbench.state.repos) {
        PastedTargets.read(query, workbench.gitHubRepos, workbench.state.repos.keys)
    }
    val folder = remember(query, target) { if (target == null) FolderTarget.read(query) else null }
    var folderFailed by remember(query) { mutableStateOf(false) }
    val searching = target == null && folder == null
    val inputs = rememberSwitcherInputs(workbench, if (searching) query else "", state.scope)
    val facts = produceState(LinkFacts(), target) { value = LinkFacts(); target?.let { value = LinkFactsReader.read(workbench.data, it) } }.value
    return SwitcherView(
        target = target,
        folder = folder,
        folderFailed = folderFailed,
        sections = if (searching) SwitcherSearch.sections(query, state.scope, inputs) else emptyList(),
        options = target?.let(OpenAs::of).orEmpty(),
        facts = facts,
        onFolderFailed = { folderFailed = true },
    )
}
