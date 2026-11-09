package dev.chieppa.arc.core.actions

/** An open pull request of the person's in [repo] (`owner/name`): its number, head commit, and whether they wrote it. */
data class MyPull(val repo: String, val number: Int, val headSha: String?, val authored: Boolean)
