package dev.chieppa.arc.install

import java.nio.file.Path

/** What a Windows shortcut holds: the program it runs with its [arguments], where it runs, its icon and its AppUserModelID. */
data class ShortcutSpec(
    val target: Path,
    val arguments: String,
    val workingDirectory: Path,
    val icon: Path,
    val description: String,
    val appId: String,
)
