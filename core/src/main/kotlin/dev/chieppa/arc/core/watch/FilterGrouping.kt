package dev.chieppa.arc.core.watch

/** How a saved filter's pull requests are grouped in its tab. */
enum class FilterGrouping {
    /** Under each pull request's first label; those without one last. */
    Label,

    /** Under each pull request's repo. */
    Repo,

    /** One list, most recently updated first. */
    None,
}
