package dev.chieppa.arc.host.github

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.Host

/** GitHub as a [Host]. */
object GitHubHost : Host {
    override val domain: String = GitHubLinkSyntax.DOMAIN

    override fun sourceFromLink(link: String): Source? = GitHubLinks.parse(link)
}
