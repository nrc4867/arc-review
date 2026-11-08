package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.line_new_comment
import dev.chieppa.arc.design.generated.staged_separator
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.Menu
import dev.chieppa.stretch.frame.MenuDivider
import dev.chieppa.stretch.frame.MenuItem
import org.jetbrains.compose.resources.stringResource

/**
 * The menu under a clicked line that already has [threads]: a new comment there, which [onNew] starts, or one of the
 * threads, staged ones among them, opened in place.
 */
@Composable
fun LineChooser(workbench: Workbench, threads: List<ReviewThread>, onNew: () -> Unit) {
    val views = workbench.threadViews
    val dismiss = { views.choosing = null }
    val separator = stringResource(Res.string.staged_separator)
    // The menu's text must not join the code's selection: it lives in a popup, outside the code's layout.
    Box {
        DisableSelection {
            Menu(true, dismiss) {
                MenuItem(stringResource(Res.string.line_new_comment), { dismiss(); onNew() }, icon = ArcIcons.Comment)
                MenuDivider()
                for (thread in threads) {
                    val (state, icon) = threadChip(thread.state)
                    val first = thread.messages.firstOrNull()?.takeIf { it.body.isNotBlank() }
                    val peek = first?.let { "${it.author}: ${it.body.lineSequence().first()}" }
                    MenuItem(listOfNotNull(state, peek).joinToString(separator), { dismiss(); views.open(thread.id, thread.state) }, icon = icon)
                }
            }
        }
    }
}
