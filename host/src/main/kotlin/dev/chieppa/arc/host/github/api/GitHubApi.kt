package dev.chieppa.arc.host.github.api

/** GitHub's API addresses and the headers every request carries. */
object GitHubApi {
    const val BASE = "https://api.github.com"
    const val GRAPHQL = "$BASE/graphql"
    const val NOTIFICATIONS = "$BASE/notifications"

    const val GET = "GET"
    const val POST = "POST"

    const val AUTHORIZATION = "Authorization"
    const val ACCEPT = "Accept"
    const val VERSION = "X-GitHub-Api-Version"
    const val USER_AGENT = "User-Agent"
    const val CONTENT_TYPE = "Content-Type"
    const val IF_NONE_MATCH = "If-None-Match"
    const val IF_MODIFIED_SINCE = "If-Modified-Since"
    const val ETAG = "ETag"
    const val LAST_MODIFIED = "Last-Modified"
    const val POLL_INTERVAL = "X-Poll-Interval"

    const val BEARER = "Bearer "
    const val JSON = "application/vnd.github+json"
    const val API_VERSION = "2022-11-28"
    const val AGENT = "arc-review"
    const val JSON_BODY = "application/json"

    const val OK = 200
    const val NOT_MODIFIED = 304
    const val UNAUTHORIZED = 401
}
