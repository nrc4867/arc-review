package dev.chieppa.arc.ui.hints

import dev.chieppa.arc.codeintel.model.CodePosition
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HoverAnswer
import dev.chieppa.arc.codeintel.model.HoverRequest
import dev.chieppa.arc.codeintel.model.SymbolHint
import dev.chieppa.arc.ui.navigation.NavigationController
import dev.chieppa.arc.ui.navigation.NavigationTargets
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.services.LinkOpener
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * What symbol cards ask: the hover answer for a name, worked out off the UI thread, the lines a hint's file has
 * changed, and where a click in a card goes, a definition or a doc comment's link.
 */
class HintController(
    private val state: WorkspaceState,
    data: ReviewData,
    private val navigation: NavigationController,
    private val links: LinkOpener,
    private val scope: CoroutineScope,
    private val dispatchers: ArcDispatchers,
) {
    private val changes = HintChanges(state, data)

    /** The answer for the name at [line], [column] of [document] with the most to show: one with symbols first. */
    suspend fun hover(document: DocumentRef, line: Int, column: Int): HoverAnswer? = withContext(dispatchers.default) {
        val sources = NavigationTargets.scopes(NavigationTargets.of(state))
        val answers = navigation.service.router.hovers(HoverRequest(document, CodePosition(line, column), sources))
        answers.firstOrNull { it.symbols.isNotEmpty() } ?: answers.firstOrNull { it.markdown.isNotBlank() }
    }

    /** The lines [hint]'s file adds in its source's change. */
    suspend fun changed(hint: SymbolHint): Set<Int> = withContext(dispatchers.io) { changes.added(hint.target) }

    /** Goes to [hint]'s definition. */
    fun open(hint: SymbolHint) = navigation.open(hint.target)

    /** Follows a link in [hint]'s doc comment: a link to a symbol goes to its definition, any other opens in the browser. */
    fun follow(link: String, hint: SymbolHint) {
        if (!link.startsWith(SYMBOL_LINK)) {
            links.open(link)
            return
        }
        scope.launch {
            val target = withContext(dispatchers.default) {
                navigation.service.symbolLink(link.removePrefix(SYMBOL_LINK), hint.target.location.document)
            }
            target?.let(navigation::open)
        }
    }

    private companion object {
        const val SYMBOL_LINK = "symbol:"
    }
}
