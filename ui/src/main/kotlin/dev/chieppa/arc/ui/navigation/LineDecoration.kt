package dev.chieppa.arc.ui.navigation

import dev.chieppa.stretch.code.CodeDecorationStyle
import dev.chieppa.stretch.code.CodeRange

/** A decoration cut to one line, ready for either viewer. */
data class LineDecoration(val range: CodeRange, val style: CodeDecorationStyle, val tooltip: String?)
