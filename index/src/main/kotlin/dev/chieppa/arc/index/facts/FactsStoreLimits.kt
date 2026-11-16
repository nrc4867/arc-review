package dev.chieppa.arc.index.facts

/** How much [DiskFactsStore] keeps. */
object FactsStoreLimits {
    /** The most bytes of facts kept on disk. */
    const val MAX_BYTES = 512L shl 20

    /** The share of [MAX_BYTES] trimming leaves, so it runs seldom. */
    const val TRIM_SHARE = 0.8
}
