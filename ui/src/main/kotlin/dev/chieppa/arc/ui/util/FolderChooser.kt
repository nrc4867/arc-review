package dev.chieppa.arc.ui.util

import androidx.compose.runtime.staticCompositionLocalOf

/** Asks the person for a folder in a dialog titled [title], opening in [start] when it names one; null when they cancel. */
fun interface FolderChooser {
    suspend fun choose(title: String, start: String?): String?
}

/** The folder dialog the window's path fields browse with: the system's, or a test's stand-in. */
val LocalFolderChooser = staticCompositionLocalOf<FolderChooser> { SystemFolderChooser }
