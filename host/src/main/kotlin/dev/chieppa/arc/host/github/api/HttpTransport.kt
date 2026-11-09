package dev.chieppa.arc.host.github.api

import java.io.InputStream

/** Sends requests to GitHub: the JDK's HTTP client in the app, recorded answers in tests. */
fun interface HttpTransport {
    suspend fun send(request: ApiRequest): ApiResponse

    /**
     * Sends [request] and hands its status and body to [read] as the body arrives, so a large one is never held whole.
     * A redirect to where GitHub keeps logs and artifacts is followed without the token.
     */
    suspend fun <T> stream(request: ApiRequest, read: (Int, InputStream) -> T): T {
        val response = send(request)
        return read(response.status, response.body.byteInputStream())
    }
}
