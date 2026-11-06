package dev.chieppa.arc.store.settings

/**
 * The person's settings for the app, as stored. A null value has not been chosen, and the app uses its default.
 * [themeId] is a Stretch theme's id; [interfaceSize] is the interface size, 1 being normal; [lastReviewId] is the review
 * open when the app last closed.
 */
data class AppSettings(
    val themeId: String? = null,
    val interfaceSize: Float? = null,
    val lastReviewId: String? = null,
)
