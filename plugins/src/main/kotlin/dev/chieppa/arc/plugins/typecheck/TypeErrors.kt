package dev.chieppa.arc.plugins.typecheck

import dev.chieppa.arc.plugins.host.PluginDiagnostic

/** A plugin's type errors: the first ones, [shown] on its row, and how many there are in all, its [total]. */
data class TypeErrors(val shown: List<PluginDiagnostic> = emptyList(), val total: Int = 0)
