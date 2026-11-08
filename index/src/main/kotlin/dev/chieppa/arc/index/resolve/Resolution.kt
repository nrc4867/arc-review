package dev.chieppa.arc.index.resolve

import dev.chieppa.arc.index.symbols.DefSite

/**
 * What a use resolves to: the [confident] candidates (in scope, sharing the best score), the [filtered] rest (in scope
 * but scored lower, or out of scope), each best first.
 */
data class Resolution(val confident: List<Ranked>, val filtered: List<Ranked>) {
    fun isConfident(site: DefSite): Boolean = confident.any { it.site.sameAs(site) }

    fun mentions(site: DefSite): Boolean = isConfident(site) || filtered.any { it.site.sameAs(site) }

    companion object {
        val None = Resolution(emptyList(), emptyList())
    }
}
