package dev.chieppa.arc.core.conversation

/**
 * A force push of a pull request's branch by [actor], with their picture's address where the host gives one, from
 * [before] to [after] at [at] (epoch seconds). The host may no longer know [before].
 */
data class HeadForcePush(val actor: String, val before: String?, val after: String?, val at: Long, val avatar: String? = null)
