package dev.chieppa.arc.core.start

/** The start screen's two columns: the wide main one and the narrower side one. */
enum class StartColumn {
    Main,
    Side,
    ;

    /** The column across from this one. */
    val other: StartColumn get() = if (this == Main) Side else Main
}
