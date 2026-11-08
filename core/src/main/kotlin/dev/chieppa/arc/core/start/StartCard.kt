package dev.chieppa.arc.core.start

/**
 * A card on the start screen. Its [id] is namespaced by whoever contributes it (`arc.recent`, `speckit.specs`). It shows
 * on [screens], in [column], [before] or [after] the card of that id there, else at the column's end. It holds a
 * [title] with an [icon] name and a [header] link beside it, its [rows], [links] in its footer and [menu] items after
 * the menu's own.
 */
data class StartCard(
    val id: String,
    val title: String,
    val screens: Set<StartScreenKind>,
    val column: StartColumn = StartColumn.Main,
    val icon: String? = null,
    val rows: List<StartRow> = emptyList(),
    val links: List<StartLink> = emptyList(),
    val menu: List<StartMenuItem> = emptyList(),
    val before: String? = null,
    val after: String? = null,
    val header: StartLink? = null,
)
