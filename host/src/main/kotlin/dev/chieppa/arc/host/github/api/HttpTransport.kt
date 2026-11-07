package dev.chieppa.arc.host.github.api

/** Sends requests to GitHub: the JDK's HTTP client in the app, recorded answers in tests. */
fun interface HttpTransport {
    suspend fun send(request: ApiRequest): ApiResponse
}
