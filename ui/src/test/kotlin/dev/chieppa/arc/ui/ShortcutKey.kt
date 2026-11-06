package dev.chieppa.arc.ui

import androidx.compose.ui.input.key.Key

/** The key that makes a chord's Ctrl on this platform: Command on macOS. */
internal val shortcutKey: Key
    get() = if (System.getProperty("os.name").orEmpty().startsWith("Mac")) Key.MetaLeft else Key.CtrlLeft
