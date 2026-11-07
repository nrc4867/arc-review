package dev.chieppa.arc.git.source

/**
 * What the host says about a pull request that reading it needs: the branch it merges into, the commits its head and
 * that branch are at now, when known, and its title.
 */
data class PullRequestRefs(val baseBranch: String, val headOid: String? = null, val baseOid: String? = null, val title: String? = null)
