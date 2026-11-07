package dev.chieppa.arc.host.github

import dev.chieppa.arc.host.github.api.ApiRequest
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.HttpTransport
import dev.chieppa.arc.host.github.auth.GitHubTokens
import dev.chieppa.arc.host.github.auth.TokenSource

/** A transport that answers from [answers] in turn and keeps every request it was sent. */
class RecordedTransport(private val answers: MutableList<ApiResponse>) : HttpTransport {
    val requests = mutableListOf<ApiRequest>()

    override suspend fun send(request: ApiRequest): ApiResponse {
        requests += request
        return answers.removeFirst()
    }
}

/** Test data and clients for GitHub tests, which never reach the live API. */
object FakeGitHub {
    const val TOKEN = "test-token"

    fun resource(name: String): String = FakeGitHub::class.java.getResource("/github/$name")!!.readText()

    fun client(transport: HttpTransport, token: String? = TOKEN): GitHubClient = GitHubClient(GitHubTokens(listOf(TokenSource { token })), transport)
}
