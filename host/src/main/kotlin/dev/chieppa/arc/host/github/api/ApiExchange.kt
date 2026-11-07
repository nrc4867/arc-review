package dev.chieppa.arc.host.github.api

/** One HTTP request to GitHub: its method, full URL, headers and body. */
data class ApiRequest(val method: String, val url: String, val headers: Map<String, String> = emptyMap(), val body: String? = null)

/** GitHub's answer: its status, headers by lowercase name, and body. */
data class ApiResponse(val status: Int, val headers: Map<String, String> = emptyMap(), val body: String = "") {
    fun header(name: String): String? = headers[name.lowercase()]
}
