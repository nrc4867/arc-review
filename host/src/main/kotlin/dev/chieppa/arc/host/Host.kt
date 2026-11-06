package dev.chieppa.arc.host

import dev.chieppa.arc.core.source.Source

/**
 * Where a repo's pull requests, comments and checks live. Everything host-specific sits behind this interface;
 * GitHub is the one host so far.
 */
interface Host {
    /** The host's address, such as `github.com`. */
    val domain: String

    /** The source a link to this host names, such as a pull request or a commit page, or null when it names none. */
    fun sourceFromLink(link: String): Source?
}
