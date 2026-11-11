package dev.chieppa.arc.plugins.net

import dev.chieppa.arc.plugins.protocol.FetchParams
import dev.chieppa.arc.plugins.protocol.FetchResult

/** Makes the HTTP requests plugins ask for. Throws [java.io.IOException] when one cannot be made. */
fun interface HttpFetcher {
    suspend fun fetch(request: FetchParams): FetchResult
}
