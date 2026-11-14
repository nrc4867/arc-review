package dev.chieppa.arc.core.activity

/**
 * A log of its own in the Console, as an editor's output channels are: Arc's git, GitHub and downloads, and one per
 * plugin that writes, by [id]; a plugin's carries its [pluginTitle].
 */
data class ActivityChannel(val id: String, val pluginTitle: String? = null) {
    val isPlugin: Boolean get() = id.startsWith(PLUGIN_PREFIX)

    companion object {
        val GIT = ActivityChannel("git")
        val GITHUB = ActivityChannel("github")
        val DOWNLOADS = ActivityChannel("downloads")

        /** Arc's own channels, in the order the Console offers them. */
        val ARC: List<ActivityChannel> = listOf(GIT, GITHUB, DOWNLOADS)

        private const val PLUGIN_PREFIX = "plugin:"

        /** The channel plugin [id] writes to, titled [title]. */
        fun plugin(id: String, title: String): ActivityChannel = ActivityChannel(PLUGIN_PREFIX + id, title)

        /** The channel Arc's operations of [kind] go to. */
        fun of(kind: ActivityKind): ActivityChannel = when (kind) {
            ActivityKind.Git -> GIT
            ActivityKind.GitHub -> GITHUB
            ActivityKind.Download, ActivityKind.Update, ActivityKind.Output -> DOWNLOADS
        }
    }
}
