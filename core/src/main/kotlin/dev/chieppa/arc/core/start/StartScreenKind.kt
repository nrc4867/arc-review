package dev.chieppa.arc.core.start

/** Which start screen shows while the pane grid has no tab, by how many sources are selected in the strip. */
enum class StartScreenKind {
    NothingOpen,
    OneSource,
    SeveralSources,
    ;

    companion object {
        /** The screen for [selected] sources selected: none, one, or two and more. */
        fun of(selected: Int): StartScreenKind = when {
            selected <= 0 -> NothingOpen
            selected == 1 -> OneSource
            else -> SeveralSources
        }
    }
}
