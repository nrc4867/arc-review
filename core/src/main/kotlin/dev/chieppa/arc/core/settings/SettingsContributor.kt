package dev.chieppa.arc.core.settings

/** The [sections] one contributor gives the Settings page, with its [id]: [ARC] for Arc's own, else a plugin's id. */
data class SettingsContributor(val id: String, val sections: List<SettingsSection>) {
    companion object {
        /** The id Arc's own sections are contributed under. */
        const val ARC = "arc"
    }
}
