package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.net.HttpFetcher
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.FetchParams
import dev.chieppa.arc.plugins.protocol.FetchResult
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException
import java.io.IOException

/** `app.fetch` for one plugin: Arc makes the request through [fetcher], only to a host its [capabilities] name. */
internal class FetchCalls(private val fetcher: HttpFetcher, private val capabilities: () -> Capabilities) {
    suspend fun fetch(params: FetchParams): FetchResult {
        CapabilityGate.checkHost(params.url, capabilities())
        return try {
            fetcher.fetch(params)
        } catch (e: IOException) {
            throw RpcException(RpcError(ErrorCodes.FAILED, e.message ?: e.toString()))
        }
    }
}
