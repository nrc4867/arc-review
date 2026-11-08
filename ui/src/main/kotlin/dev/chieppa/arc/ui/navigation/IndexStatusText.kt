package dev.chieppa.arc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.status_index
import dev.chieppa.arc.design.generated.status_index_files
import dev.chieppa.arc.design.generated.status_index_sources
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import java.text.NumberFormat

/** The status bar's index item, `index: 4 sources, 1,284 files`, once the index holds a source. */
@Composable
fun IndexStatusItem(navigation: NavigationController) {
    val status by navigation.service.status.collectAsState()
    if (status.sources == 0) return
    val sources = pluralStringResource(Res.plurals.status_index_sources, status.sources, status.sources)
    val files = pluralStringResource(Res.plurals.status_index_files, status.files, NumberFormat.getIntegerInstance().format(status.files))
    StatusItem(stringResource(Res.string.status_index, sources, files))
}
