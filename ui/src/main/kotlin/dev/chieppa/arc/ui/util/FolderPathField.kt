package dev.chieppa.arc.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.path_browse
import dev.chieppa.arc.design.generated.path_choose_folder
import dev.chieppa.stretch.ui.PathField
import org.jetbrains.compose.resources.stringResource

/**
 * A field for a folder's path whose folder icon opens the [LocalFolderChooser], starting in the folder already typed,
 * and fills the field with the folder chosen.
 */
@Composable
fun FolderPathField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, error: String? = null) {
    val chooser = LocalFolderChooser.current
    val title = stringResource(Res.string.path_choose_folder)
    PathField(
        value,
        onValueChange,
        stringResource(Res.string.path_browse),
        { chooser.choose(title, TypedPath.read(value)?.toString()) },
        modifier,
        placeholder = placeholder,
        error = error,
    )
}
