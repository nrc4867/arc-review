package dev.chieppa.arc.ui.files

import dev.chieppa.arc.ui.util.ArcAlphas
import dev.chieppa.arc.ui.util.SourceLabels
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import dev.chieppa.arc.ui.menus.TreeRowMenu
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.stretch.frame.ContextMenuPopup
import dev.chieppa.stretch.frame.TabItem
import dev.chieppa.stretch.frame.paneDragSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.files_set_aside
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.tree.FileTreeItem
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.chord
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.Tree
import dev.chieppa.stretch.ui.treeParents
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

/**
 * Every source's changed files, grouped by source and folder. Selecting one file opens its change; Enter or a
 * double-click opens it in a new tab. The file being read is kept selected, and a source shown with no folder open
 * opens them all. The marking keys act on the selection while the tree has the keyboard. A right-click, or Shift F10,
 * opens the row's menu. A file drags into the pane grid as a tab: onto a pane, an edge to split it, or away into a
 * window of its own.
 */
@Composable
fun FileTree(workbench: Workbench, modifier: Modifier = Modifier) {
    val state = workbench.state
    val roots = ReviewTree.roots(state, workbench.sinceReview)
    val aside = stringResource(Res.string.files_set_aside)
    val treeState = workbench.treeState
    var menu by remember { mutableStateOf<Pair<MenuTarget.OfRows, Offset>?>(null) }
    LaunchedEffect(treeState) {
        snapshotFlow { treeState.selection }.distinctUntilChanged().collect { keys ->
            if (menu != null) return@collect
            val (ref, path) = keys.singleOrNull()?.let(TreeKeys::fileOf) ?: return@collect
            SourceRefs.parse(ref)?.let { workbench.panes.openChange(it, path) }
        }
    }
    val active = state.active
    val loaded = active?.let(state::snapshot) != null
    LaunchedEffect(active, loaded) {
        if (active == null || !loaded) return@LaunchedEffect
        val opened = treeState.expanded.any { it is String && it != TreeKeys.source(active) && TreeKeys.belongsTo(it, active) }
        if (!opened) treeState.expanded += treeParents(roots.filter { it.item.source == active }) { it.key }
    }
    val reading = workbench.marks.current
    LaunchedEffect(reading, loaded) {
        val (source, path) = reading ?: return@LaunchedEffect
        if (!loaded) return@LaunchedEffect
        val key = TreeKeys.file(source, path)
        treeState.expandTo(roots, key) { it.key }
        if (key !in treeState.selection) treeState.selection = setOf(key)
    }
    val reviewKeys = remember { ArcKeys.REVIEW_KEYS }
    Box(modifier) {
        Tree(
            roots = roots,
            state = treeState,
            key = { it.key },
            label = { item ->
                when (item) {
                    is FileTreeItem.SourceRow -> SourceLabels.of(item.source) + if (item.title.isEmpty()) "" else "  " + item.title
                    is FileTreeItem.FolderRow -> item.label
                    is FileTreeItem.FileRow -> item.file.name
                    is FileTreeItem.AsideRow -> aside
                }
            },
            modifier = Modifier.fillMaxSize()
                .onFocusChanged { workbench.treeFocused = it.hasFocus }
                .onPreviewKeyEvent { event ->
                    val chord = event.chord() ?: return@onPreviewKeyEvent false
                    if (chord !in reviewKeys) return@onPreviewKeyEvent false
                    workbench.keymap.resolve(chord, null)?.run()
                    true
                },
            icon = { item ->
                when (item) {
                    is FileTreeItem.SourceRow -> sourceIcon(item.source)
                    is FileTreeItem.FolderRow -> if (item.key in treeState.expanded) ArcIcons.FolderOpen else StretchIcons.Folder
                    is FileTreeItem.FileRow -> null
                    is FileTreeItem.AsideRow -> ArcIcons.SetAside
                }
            },
            onActivate = { item ->
                when (item) {
                    is FileTreeItem.FileRow -> workbench.panes.openChange(item.source, item.file.path, newTab = true)
                    is FileTreeItem.SourceRow -> workbench.activateSource(item.source)
                    else -> Unit
                }
            },
            rowModifier = { item ->
                when {
                    item is FileTreeItem.FileRow ->
                        Modifier.paneDragSource(workbench.dropTarget) { TabItem(item.key, item.file.name, StretchIcons.File, subtitle = item.source.repo.value) }
                    item is FileTreeItem.SourceRow && item.ended -> Modifier.alpha(ArcAlphas.ENDED)
                    else -> Modifier
                }
            },
            onContextMenu = { item, at -> menu = MenuTarget.OfRows(item, treeState.selection) to at },
            leading = { item -> if (item is FileTreeItem.FileRow) StatusLetter(item.file.status) },
            trailing = { item -> TreeRowEnd(item, workbench) },
        )
        ContextMenuPopup(menu?.second, { menu = null }) { close -> menu?.first?.let { TreeRowMenu(workbench, it, close) } }
    }
}
