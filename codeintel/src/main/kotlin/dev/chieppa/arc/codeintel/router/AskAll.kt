package dev.chieppa.arc.codeintel.router

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Asks every one of [providers] at once and keeps the answers in their order; one that fails is left out. */
internal suspend fun <P, A : Any> askAll(providers: List<P>, ask: suspend (P) -> A?): List<A> = coroutineScope {
    providers.map { provider ->
        async {
            try {
                ask(provider)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    }.awaitAll().filterNotNull()
}
