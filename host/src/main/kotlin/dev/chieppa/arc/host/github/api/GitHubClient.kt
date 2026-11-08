package dev.chieppa.arc.host.github.api

import dev.chieppa.arc.host.github.auth.GitHubTokens
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.io.IOException

/**
 * Requests to GitHub with the person's token: GraphQL queries and mutations, conditional REST GETs and REST POSTs.
 * Every failure is a [GitHubException]; a refused token is dropped so the next request asks for it again.
 */
class GitHubClient(private val tokens: GitHubTokens = GitHubTokens(), private val transport: HttpTransport = JdkHttpTransport()) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Whether a token is to hand, without asking GitHub. */
    fun hasToken(): Boolean = tokens.token() != null

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

    /** A REST GET of [url], conditional on [etag] or [lastModified] from an earlier answer; a 304 comes back as is. */
    suspend fun get(url: String, etag: String? = null, lastModified: String? = null): ApiResponse {
        val headers = buildMap {
            etag?.let { put(GitHubApi.IF_NONE_MATCH, it) }
            lastModified?.let { put(GitHubApi.IF_MODIFIED_SINCE, it) }
        }
        return send(GitHubApi.GET, url, headers, null)
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
        val headers = mapOf(
            GitHubApi.AUTHORIZATION to GitHubApi.BEARER + token,
            GitHubApi.ACCEPT to GitHubApi.JSON,
            GitHubApi.VERSION to GitHubApi.API_VERSION,
            GitHubApi.USER_AGENT to GitHubApi.AGENT,
        ) + extra
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
