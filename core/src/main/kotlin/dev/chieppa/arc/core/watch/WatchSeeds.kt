package dev.chieppa.arc.core.watch

/**
 * What a first run starts with: a filter for the pull requests waiting on the person's review and one for their own,
 * and notifications for comments on their pull requests and for mentions, in any repo. Names come from the caller, so
 * they can be the app's own words.
 */
object WatchSeeds {
    const val REVIEW_REQUESTED = "is:open review-requested:@me"
    const val MINE = "is:open author:@me"

    fun create(reviewRequestedName: String, mineName: String): Watchlist {
        val requested = SavedFilter("${Watchlist.FILTER_PREFIX}1", reviewRequestedName, REVIEW_REQUESTED)
        val mine = SavedFilter("${Watchlist.FILTER_PREFIX}2", mineName, MINE)
        return Watchlist(
            filters = listOf(requested, mine),
            rules = listOf(
                NotifyRule.CommentsOnMyPullRequests("${Watchlist.RULE_PREFIX}1"),
                NotifyRule.Mentions("${Watchlist.RULE_PREFIX}2"),
            ),
        )
    }
}
