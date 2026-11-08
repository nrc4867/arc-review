package dev.chieppa.arc.core.start

/**
 * One row of a start card: an [icon] name from Arc's or Stretch's icons, a [title], an optional [subtitle] under it and
 * one [trailing] item. A click runs [action]; a right-click offers [menu].
 */
data class StartRow(
    val title: String,
    val icon: String? = null,
    val subtitle: String? = null,
    val trailing: StartTrailing? = null,
    val action: StartAction? = null,
    val menu: List<StartMenuItem> = emptyList(),
)
