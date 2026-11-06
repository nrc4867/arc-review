package dev.chieppa.arc.core.review

/** A review's id, stable while its name changes. */
@JvmInline
value class ReviewId(val value: String) {
    init {
        require(value.isNotBlank()) { "A review id is not blank" }
    }

    override fun toString(): String = value
}
