package dev.chieppa.arc.host.github

import dev.chieppa.arc.core.source.Source

/**
 * What a github.com link points at: the [source] it names, its repo named `owner/name`, and the number of the pull
 * request it sits in, [pullRequest], for a pull request or one of its commits.
 */
data class GitHubLink(val source: Source, val pullRequest: Int? = null)
