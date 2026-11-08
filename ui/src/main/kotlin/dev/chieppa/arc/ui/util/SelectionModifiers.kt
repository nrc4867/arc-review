package dev.chieppa.arc.ui.util

import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed

/** Whether a click with these keys held adds to a selection or takes from it: Ctrl, or Cmd on macOS, as Stretch's trees take it. */
val PointerKeyboardModifiers.extendsSelection: Boolean get() = isCtrlPressed || isMetaPressed
