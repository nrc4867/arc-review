package dev.chieppa.arc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.chieppa.arc.codeintel.model.CodeDecoration
import dev.chieppa.arc.codeintel.model.DecorationRequest
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.withContext

/** The decorations [document] draws, asked again as providers come and go and as navigation marks names. */
@Composable
fun rememberDecorations(workbench: Workbench, document: DocumentRef?, lineLength: (Int) -> Int?): List<LineDecoration> {
    val navigation = workbench.navigation
    val providers by navigation.service.intel.decorations.providers.collectAsState()
    val provided by produceState(emptyList<CodeDecoration>(), document, providers) {
        value = if (document == null || providers.isEmpty()) {
            emptyList()
        } else {
            withContext(workbench.dispatchers.default) { navigation.service.router.decorations(DecorationRequest(document)) }
        }
    }
    val focus = navigation.focus
    return remember(document, focus, provided) {
        if (document == null) emptyList() else PaneDecorations.of(document, focus, provided, lineLength)
    }
}
