package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** The last [capacity] lines a plugin wrote to its log, oldest first. */
class PluginLog(private val capacity: Int = CAPACITY) {
    private val kept = MutableStateFlow<List<String>>(emptyList())

    val lines: StateFlow<List<String>> get() = kept

    fun add(line: String) {
        kept.value = (kept.value + line).takeLast(capacity)
    }

    private companion object {
        const val CAPACITY = 500
    }
}
