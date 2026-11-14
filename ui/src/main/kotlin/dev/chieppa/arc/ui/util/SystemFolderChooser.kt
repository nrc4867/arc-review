package dev.chieppa.arc.ui.util

import dev.chieppa.stretch.ui.SystemFileDialogs

/** The system's own folder dialog, over the active window. */
object SystemFolderChooser : FolderChooser {
    override suspend fun choose(title: String, start: String?): String? = SystemFileDialogs.chooseFolder(title, start)
}
