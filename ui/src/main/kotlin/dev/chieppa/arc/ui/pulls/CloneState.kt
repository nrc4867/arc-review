package dev.chieppa.arc.ui.pulls

/** Where the app's own clone of a pull request's repo stands, while there is no clone of it to read. */
enum class CloneState {
    Cloning,
    Failed,
}
