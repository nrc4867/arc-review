package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.plugins.api.PluginIcons
import dev.chieppa.stretch.theme.StretchIcons

/** The icon each of [PluginIcons.NAMES] names: Arc's for Arc's functions, Stretch's for the rest. */
object PluginIconNames {
    private val icons: Map<String, ImageVector> by lazy {
        mapOf(
            "branch" to ArcIcons.Branch,
            "check" to StretchIcons.Check,
            "close" to StretchIcons.Close,
            "code" to StretchIcons.Code,
            "comment" to ArcIcons.Comment,
            "commit" to ArcIcons.Commit,
            "console" to StretchIcons.Command,
            "file" to StretchIcons.File,
            "filter" to ArcIcons.Filter,
            "folder" to StretchIcons.Folder,
            "info" to StretchIcons.Info,
            "link" to StretchIcons.Link,
            "note" to ArcIcons.Note,
            "play" to StretchIcons.Play,
            "plugin" to ArcIcons.Plugin,
            "plus" to StretchIcons.Plus,
            "pull-request" to ArcIcons.PullRequest,
            "refresh" to ArcIcons.Refresh,
            "runs" to ArcIcons.Runs,
            "search" to StretchIcons.Search,
            "settings" to StretchIcons.Settings,
            "spec" to ArcIcons.Spec,
            "stop" to ArcIcons.Stop,
            "tag" to ArcIcons.Tag,
            "warning" to ArcIcons.Warning,
        )
    }

    /** The names this maps, which must be every name plugins may use. */
    val names: Set<String> get() = icons.keys

    /** The icon [name] names, or null for none. */
    fun of(name: String?): ImageVector? = name?.let(icons::get)

    /** The icon [name] names, or the plugin icon. */
    fun orPlugin(name: String?): ImageVector = of(name) ?: ArcIcons.Plugin
}
