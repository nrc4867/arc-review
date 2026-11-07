package dev.chieppa.arc.core.watch

/** Something the person wants a desktop notification for, on or off. Each kind is a subtype. */
sealed interface NotifyRule {
    val id: String
    val on: Boolean

    /** The rule switched [on] or off. */
    fun switched(on: Boolean): NotifyRule = when (this) {
        is CommentsOnMyPullRequests -> copy(on = on)
        is Mentions -> copy(on = on)
        is NewInFilter -> copy(on = on)
        is NewLabelled -> copy(on = on)
    }

    /** New comments on pull requests the person opened, in any repo. */
    data class CommentsOnMyPullRequests(override val id: String, override val on: Boolean = true) : NotifyRule

    /** The person, or a team of theirs, mentioned anywhere. */
    data class Mentions(override val id: String, override val on: Boolean = true) : NotifyRule

    /** A pull request the saved filter [filter] finds that it did not before. */
    data class NewInFilter(override val id: String, val filter: String, override val on: Boolean = true) : NotifyRule

    /** A new open pull request labelled [label] in any of [repos], each `owner/name`. */
    data class NewLabelled(override val id: String, val label: String, val repos: List<String>, override val on: Boolean = true) : NotifyRule
}
