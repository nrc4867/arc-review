package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Search the files of [context]'s source for [pattern], a regular expression, at most [limit] hits. */
@Serializable
data class GitGrepParams(val pattern: String, val context: CallContext = CallContext(), val limit: Int = DEFAULT_LIMIT) {
    companion object {
        const val DEFAULT_LIMIT = 500
    }
}
