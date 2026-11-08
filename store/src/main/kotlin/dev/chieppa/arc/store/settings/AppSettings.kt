package dev.chieppa.arc.store.settings

import dev.chieppa.arc.core.start.StartLayout

/**
 * The person's settings for the app, as stored. A null value has not been chosen, and the app uses its default.
 * [themeId] is a Stretch theme's id; [interfaceSize] is the interface size, 1 being normal; [lastReviewId] names the
 * saved review the workspace starts from when there is no workspace file; [wrapLines] is whether panes wrap long lines
 * unless a pane says otherwise; [wholeLineHighlight] is whether changed lines are highlighted whole rather than on their
 * gutter only; [startLayout] is what the person hid and moved on each start screen.
 */
data class AppSettings(
    val themeId: String? = null,
    val interfaceSize: Float? = null,
    val lastReviewId: String? = null,
    val wrapLines: Boolean? = null,
    val wholeLineHighlight: Boolean? = null,
    val startLayout: StartLayout = StartLayout(),
)
