package dev.chieppa.arc.index.resolve

import dev.chieppa.arc.index.symbols.DefSite

/** A candidate definition with its [score]. */
data class Ranked(val site: DefSite, val score: Int)
