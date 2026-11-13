package dev.chieppa.arc.host.github.api

import dev.chieppa.arc.host.github.auth.GitHubTokens
import dev.chieppa.arc.host.github.auth.TokenOrigin
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import dev.chieppa.arc.host.github.actions.RestBudget
import dev.chieppa.arc.host.github.pulls.RateBudget
import java.io.IOException
import java.io.InputStream

/**
 * Requests to GitHub with the person's token: GraphQL queries and mutations, conditional REST GETs, streamed REST GETs
 * and REST POSTs. The REST budget each REST answer reports is kept.
 * Every failure is a [GitHubException]; a refused token is dropped so the next request asks for it again.
 */
class GitHubClient(private val tokens: GitHubTokens = GitHubTokens(), private val transport: HttpTransport = JdkHttpTransport()) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Whether a token is to hand, without asking GitHub. */
    fun hasToken(): Boolean = tokens.token() != null

    /** Where the token in use came from, or null when there is none. */
    fun tokenOrigin(): TokenOrigin? = tokens.tokenOrigin()

    /** The person's token, for a request to GitHub this client does not send itself, such as a picture. */
    fun token(): String? = tokens.token()

    /** The `data` of the GraphQL [query]. */
    suspend fun graphql(query: String): JsonObject = graphql(query, null)

    /** The `data` of the GraphQL [query] or mutation, sent with [variables] so no value is written into its text. */
    suspend fun graphql(query: String, variables: JsonObject?): JsonObject {
        val body = buildJsonObject {
            put(QUERY, JsonPrimitive(query))
            variables?.let { put(VARIABLES, it) }
        }.toString()
        val response = send(GitHubApi.POST, GitHubApi.GRAPHQL, mapOf(GitHubApi.CONTENT_TYPE to GitHubApi.JSON_BODY), body)
        if (response.status != GitHubApi.OK) throw GitHubException(GitHubException.Reason.Failed, "GraphQL answered ${response.status}")
        val root = parse(response.body) as? JsonObject ?: throw GitHubException(GitHubException.Reason.Failed, "GraphQL answer is not an object")
        val errors = root[ERRORS] as? JsonArray
        val data = root[DATA] as? JsonObject
        if (data == null || !errors.isNullOrEmpty()) throw GitHubException(GitHubException.Reason.Failed, "GraphQL errors: ${messages(errors) ?: NO_DATA}")
        return data
    }

    /** The REST budget the latest REST answer reported, or null before any. */
    @Volatile
    var restBudget: RateBudget? = null
        private set

    /**
     * A REST GET of [url], conditional on [etag] or [lastModified] from an earlier answer, asking for [accept] when it
     * is not GitHub's JSON; a 304 comes back as is.
     */
    suspend fun get(url: String, etag: String? = null, lastModified: String? = null, accept: String? = null): ApiResponse {
        val headers = buildMap {
            etag?.let { put(GitHubApi.IF_NONE_MATCH, it) }
            lastModified?.let { put(GitHubApi.IF_MODIFIED_SINCE, it) }
            accept?.let { put(GitHubApi.ACCEPT, it) }
        }
        return send(GitHubApi.GET, url, headers, null).also { response -> RestBudget.of(response)?.let { restBudget = it } }
    }

    /** A REST GET of [url] whose status and body go to [read] as the body arrives. */
    suspend fun <T> stream(url: String, read: (Int, InputStream) -> T): T = stream(url, null, read)

    /** A REST GET of [url] asking for [accept] when it is not GitHub's JSON, its status and body going to [read] as the body arrives. */
    suspend fun <T> stream(url: String, accept: String?, read: (Int, InputStream) -> T): T {
        val token = tokens.token() ?: throw GitHubException(GitHubException.Reason.NoToken, "No GitHub token")
        val headers = standardHeaders(token) + listOfNotNull(accept?.let { GitHubApi.ACCEPT to it })
        return try {
            transport.stream(ApiRequest(GitHubApi.GET, url, headers), read)
        } catch (e: IOException) {
            throw GitHubException(GitHubException.Reason.Failed, "No answer: ${e.message}")
        }
    }

    /** A REST POST of the JSON [body] to [url]; any status comes back as is. */
    suspend fun post(url: String, body: String): ApiResponse =
        send(GitHubApi.POST, url, mapOf(GitHubApi.CONTENT_TYPE to GitHubApi.JSON_BODY), body)

    /** [text] as JSON. */
    fun parse(text: String): JsonElement = try {
        json.parseToJsonElement(text)
    } catch (e: SerializationException) {
        throw GitHubException(GitHubException.Reason.Failed, "Not JSON: ${e.message}")
    }

    /** The `message` of each of [errors], or the errors as they came when none has one. */
    private fun messages(errors: JsonArray?): String? {
        if (errors.isNullOrEmpty()) return null
        val texts = errors.mapNotNull { ((it as? JsonObject)?.get(MESSAGE) as? JsonPrimitive)?.content }
        return if (texts.isEmpty()) errors.toString() else texts.joinToString(MESSAGE_SEPARATOR)
    }

    private suspend fun send(method: String, url: String, extra: Map<String, String>, body: String?): ApiResponse {
        val token = tokens.token() ?: throw GitHubException(GitHubException.Reason.NoToken, "No GitHub token")
        val headers = standardHeaders(token) + extra
        val response = try {
            transport.send(ApiRequest(method, url, headers, body))
        } catch (e: IOException) {
            throw GitHubException(GitHubException.Reason.Failed, "No answer: ${e.message}")
        }
        if (response.status == GitHubApi.UNAUTHORIZED) {
            tokens.forget()
            throw GitHubException(GitHubException.Reason.Unauthorized, "GitHub refused the token")
        }
        return response
    }

    private fun standardHeaders(token: String): Map<String, String> = mapOf(
        GitHubApi.AUTHORIZATION to GitHubApi.BEARER + token,
        GitHubApi.ACCEPT to GitHubApi.JSON,
        GitHubApi.VERSION to GitHubApi.API_VERSION,
        GitHubApi.USER_AGENT to GitHubApi.AGENT,
    )

    private companion object {
        const val QUERY = "query"
        const val VARIABLES = "variables"
        const val DATA = "data"
        const val ERRORS = "errors"
        const val NO_DATA = "no data"
        const val MESSAGE = "message"
        const val MESSAGE_SEPARATOR = "; "
    }
}
