package dev.chieppa.arc.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.design.ArcIcons

/** The icon an outline row draws for a symbol's kind: a type, something callable, or a value. */
object SymbolIcons {
    private val types = setOf("class", "interface", "object", "enum", "type", "module", "table")
    private val callables = setOf("function", "method", "constructor", "procedure")

    fun of(kind: String): ImageVector = when (kind) {
        in types -> ArcIcons.SymbolClass
        in callables -> ArcIcons.SymbolFun
        else -> ArcIcons.SymbolVal
    }

    /** Whether a symbol of [kind] holds members, so the outline names its kind where a value names its type. */
    fun isType(kind: String): Boolean = kind in types
}
