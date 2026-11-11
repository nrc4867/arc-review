package dev.chieppa.arc.plugins.bundle

/** How a program ended: its exit [code] and what it printed on its standard [output] and [error]. */
data class CommandResult(val code: Int, val output: String, val error: String)
