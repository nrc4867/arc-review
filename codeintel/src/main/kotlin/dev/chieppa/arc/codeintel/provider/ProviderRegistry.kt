package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.DocumentRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The providers of one kind, highest rank first and, at equal rank, in the order they registered. Registering returns
 * a handle that takes the provider out again; [providers] changes as they come and go, so views can ask again.
 */
class ProviderRegistry<P : CodeIntelProvider> {
    private val registered = MutableStateFlow<List<P>>(emptyList())

    val providers: StateFlow<List<P>> = registered.asStateFlow()

    fun register(provider: P): AutoCloseable {
        registered.update { list -> (list + provider).sortedByDescending { it.rank } }
        return AutoCloseable { registered.update { list -> list.filterNot { it === provider } } }
    }

    /** The providers whose selector takes [document], in asking order. */
    fun matching(document: DocumentRef): List<P> = registered.value.filter { it.selector.matches(document) }
}
