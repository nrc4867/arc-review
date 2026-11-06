package dev.chieppa.arc.core.pane

/** A revision git can read a file at: a commit sha or a ref name such as `main`. */
@JvmInline
value class Revision(val value: String) {
    init {
        require(value.isNotBlank() && value.none(Char::isWhitespace)) { "Not a revision: '$value'" }
    }

    override fun toString(): String = value
}
