package dev.chieppa.arc.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Rect
import dev.chieppa.arc.codeintel.model.CodeLocation
import dev.chieppa.arc.codeintel.model.CodePosition
import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DecorationStyle
import dev.chieppa.arc.codeintel.model.DefinitionRequest
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.SourceScope
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.codeintel.model.UsagesRequest
import dev.chieppa.arc.codeintel.router.Navigation
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Code navigation in the window: keeps the index on the open sources, answers which name is under the pointer, and
 * acts on a Ctrl+click. One confident definition opens through [opener]; candidates or usages open a list under the
 * name; nothing happens for a name with no definition here. The names a navigation found are marked in the panes.
 */
class NavigationController(
    private val state: WorkspaceState,
    val service: CodeNavigation,
    private val opener: SymbolOpener,
    private val scope: CoroutineScope,
    private val dispatchers: ArcDispatchers,
) {
    /** The list open under a clicked name, if any. */
    var popup by mutableStateOf<NavigationPopup?>(null)

    /** Where the last jump went, until the pane showing it has scrolled there. */
    var reveal by mutableStateOf<NavReveal?>(null)

    /** The names the last navigation marked. */
    var focus by mutableStateOf<List<FocusMark>>(emptyList())

    /** The caret of the pane that last had one. */
    var caret by mutableStateOf<PaneCaret?>(null)

    private var usagesNext = false

    /** Keeps the index on the sources as they are read and move. */
    fun start() {
        scope.launch {
            snapshotFlow { NavigationTargets.of(state) }.distinctUntilChanged().collectLatest { targets ->
                withContext(dispatchers.default) { service.index(targets) }
            }
        }
    }

    /** The name under [line], [column] of [document], worked out off the UI thread. */
    suspend fun nameAt(document: DocumentRef, line: Int, column: Int): NameSpan? =
        withContext(dispatchers.default) { service.nameAt(document, line, column) }

    /** Makes the next symbol click list the usages of the name's definition instead of going to it. */
    fun askUsagesNext() {
        usagesNext = true
    }

    /** Takes back [askUsagesNext] when no click followed. */
    fun cancelUsagesNext() {
        usagesNext = false
    }

    /** A Ctrl+click on [name] in [document], shown in pane [paneId], whose name lies at [anchor] in the pane. */
    fun click(paneId: String, document: DocumentRef, name: NameSpan, anchor: Rect?) {
        val usages = usagesNext
        usagesNext = false
        val sources = NavigationTargets.scopes(NavigationTargets.of(state))
        val clicked = CodeLocation(document, CodeRange(CodePosition(name.line, name.start), CodePosition(name.line, name.end)))
        scope.launch {
            val request = DefinitionRequest(document, clicked.range.start, sources)
            val navigation = withContext(dispatchers.default) {
                val found = service.router.navigate(request)
                if (usages) usagesOf(found, sources) ?: found else found
            }
            when (navigation) {
                is Navigation.Jump -> {
                    focus = listOf(FocusMark(clicked, DecorationStyle.Reference), FocusMark(navigation.target.location, DecorationStyle.Definition))
                    open(navigation.target)
                }
                is Navigation.Usages -> {
                    val definition = if (usages) emptyList() else listOf(FocusMark(clicked, DecorationStyle.Definition))
                    focus = definition + navigation.answer.groups.flatMap { group -> group.usages.map { FocusMark(it.location, DecorationStyle.Reference) } }
                    popup = NavigationPopup(paneId, anchor, navigation, sources)
                }
                is Navigation.Choose -> popup = NavigationPopup(paneId, anchor, navigation, sources)
                Navigation.Nothing -> popup = null
            }
        }
    }

    /** Goes to [target], closing the list it was picked from. */
    fun open(target: SymbolTarget) {
        popup = null
        reveal = opener.open(target)
    }

    fun dismiss() {
        popup = null
    }

    /** The usages of the definition [found] leads to, when it leads to one. */
    private suspend fun usagesOf(found: Navigation, sources: List<SourceScope>): Navigation? {
        val target = when (found) {
            is Navigation.Jump -> found.target
            is Navigation.Choose -> found.answer.targets.firstOrNull { it.confidence == Confidence.Confident }
            else -> null
        } ?: return null
        val answer = service.router.usages(UsagesRequest(target.location.document, target.location.range.start, sources)) ?: return null
        return if (answer.count == 0) null else Navigation.Usages(answer)
    }
}
