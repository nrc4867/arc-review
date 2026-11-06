package dev.chieppa.arc.core.pane

/** A pane's id, unique in its review. The pane grid keys its tabs by it. */
@JvmInline
value class PaneId(val value: String) {
    init {
        require(value.isNotBlank()) { "A pane id is not blank" }
    }

    override fun toString(): String = value
}
