package dev.chieppa.arc.store.settings

/**
 * The person's settings for the app, as stored. A null value has not been chosen, and the app uses its default.
 * [themeId] is a Stretch theme's id; [interfaceSize] is the interface size, 1 being normal; [lastReviewId] is the review
 * open when the app last closed; [wrapLines] is whether panes wrap long lines unless a pane says otherwise;
 * [wholeLineHighlight] is whether changed lines are highlighted whole rather than on their gutter only.
 */
data class AppSettings(
    val themeId: String? = null,
    val interfaceSize: Float? = null,
    val lastReviewId: String? = null,
    val wrapLines: Boolean? = null,
    val wholeLineHighlight: Boolean? = null,
)
