package dev.chieppa.arc.ui.start

/** How a source's files stand: [reviewed], [changed] since they were reviewed, [aside] and still [toGo]. */
data class ReviewCounts(val reviewed: Int, val changed: Int, val aside: Int, val toGo: Int) {
    val total: Int get() = reviewed + changed + aside + toGo

    /** The files that count towards the review, set-aside ones left out. */
    val counted: Int get() = reviewed + changed + toGo
}
