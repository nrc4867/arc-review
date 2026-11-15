package dev.chieppa.arc.host.github.search

/** What a typed or pasted search came to: a search to save, or why it is none. */
sealed interface SearchReading {
    data class Found(val search: PastedSearch) : SearchReading

    /** Refused for [problem]; [detail] is the part at fault, such as a search's type or a qualifier, when there is one. */
    data class Refused(val problem: SearchProblem, val detail: String? = null) : SearchReading
}

/** Why a typed query or pasted link makes no saved filter. */
enum class SearchProblem {
    /** Nothing was typed. */
    Empty,

    /** A link to somewhere other than github.com. */
    NotGitHub,

    /** A github.com link that is no search or list of pull requests, such as one pull request. */
    NotASearch,

    /** A search link with no query in it. */
    NoQuery,

    /** A search of something other than pull requests or issues, such as repositories or code. */
    NotPullRequests,

    /** A quote opened and never closed. */
    UnclosedQuote,

    /** Parentheses that do not pair up. */
    UnbalancedParentheses,

    /** A qualifier, such as `label:`, with nothing after it. */
    EmptyQualifier,
}
