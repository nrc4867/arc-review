package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.CodeLocation
import dev.chieppa.arc.codeintel.model.DecorationStyle

/** A name the last navigation marked: a definition or one of its uses, at [location]. */
data class FocusMark(val location: CodeLocation, val style: DecorationStyle)
