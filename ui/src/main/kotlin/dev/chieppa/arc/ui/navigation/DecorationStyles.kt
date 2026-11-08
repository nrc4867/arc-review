package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.DecorationStyle
import dev.chieppa.stretch.code.CodeDecorationStyle

/** The viewer look of each decoration style a provider can ask for. */
object DecorationStyles {
    fun of(style: DecorationStyle): CodeDecorationStyle = when (style) {
        DecorationStyle.Reference -> CodeDecorationStyle.Reference
        DecorationStyle.Definition -> CodeDecorationStyle.Definition
        DecorationStyle.Accent -> CodeDecorationStyle.Accent
        DecorationStyle.Warning -> CodeDecorationStyle.Warning
        DecorationStyle.Error -> CodeDecorationStyle.Error
    }
}
