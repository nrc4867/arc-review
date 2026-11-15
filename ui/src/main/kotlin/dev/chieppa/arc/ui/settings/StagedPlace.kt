package dev.chieppa.arc.ui.settings

/** Where the staged batch, with its verdict, summary and push, sits: a card on the source's home, or a drawer on the rail. */
enum class StagedPlace(val id: String) {
    Home("home"),
    Rail("rail"),
    ;

    companion object {
        val DEFAULT = Home

        /** The place with [id], or the default when there is none. */
        fun byId(id: String?): StagedPlace = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
