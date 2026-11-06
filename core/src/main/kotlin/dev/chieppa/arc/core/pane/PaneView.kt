package dev.chieppa.arc.core.pane

/** How a pane shows its file. */
enum class PaneView {
    /** The file's change, with the lines around it. */
    Diff,

    /** The whole file, changed lines still marked. */
    FullFile,

    /** The file drawn as what it describes, such as Markdown as a page. */
    Rendered,
}
