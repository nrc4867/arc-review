package dev.chieppa.arc.core.start

/** The person's changes to each start screen, which apply over every contributor's placement and hiding. */
data class StartLayout(val screens: Map<StartScreenKind, StartScreenLayout> = emptyMap()) {
    fun of(kind: StartScreenKind): StartScreenLayout = screens[kind] ?: StartScreenLayout()

    /** This layout with [kind]'s changes replaced by [layout]; a screen left with no changes is dropped. */
    fun with(kind: StartScreenKind, layout: StartScreenLayout): StartLayout =
        StartLayout(if (layout.isDefault) screens - kind else screens + (kind to layout))
}
