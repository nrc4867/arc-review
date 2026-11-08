package dev.chieppa.arc.ui.start

/** Arc's pages on the web, which Help opens. */
object ArcLinks {
    const val REPO = "https://github.com/nrc4867/arc-review"
    const val GUIDE = "$REPO#readme"
    const val NEW_ISSUE = "$REPO/issues/new"

    /** The checks page of the pull request at [url]. */
    fun checks(url: String): String = "$url/checks"

    /** The page of the GitHub repo [fullName], `owner/name`. */
    fun repo(fullName: String): String = "https://github.com/$fullName"
}
