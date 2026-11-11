package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val SEPARATOR = ","

/** The search keywords [resource] lists, comma separated. */
@Composable
fun settingsKeywords(resource: StringResource): List<String> =
    stringResource(resource).split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
