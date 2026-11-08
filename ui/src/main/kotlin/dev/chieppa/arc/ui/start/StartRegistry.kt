package dev.chieppa.arc.ui.start

import androidx.compose.runtime.mutableStateMapOf

/**
 * The start screen's contributors, by id: Arc's own under `arc`, and each plugin's under its plugin id. The screen
 * asks every one of them and merges their answers, so Arc's cards and a plugin's go through the same path.
 */
class StartRegistry {
    private val providers = mutableStateMapOf<String, StartProvider>()

    /** Every contributor, in id order. */
    val all: List<Pair<String, StartProvider>> get() = providers.entries.sortedBy { it.key }.map { it.key to it.value }

    fun register(id: String, provider: StartProvider) {
        require(id !in providers) { "Start contributor $id is already registered" }
        providers[id] = provider
    }

    /** Takes contributor [id] out, as when its plugin stops; its cards and rows leave the screen. */
    fun unregister(id: String) {
        providers.remove(id)
    }
}
