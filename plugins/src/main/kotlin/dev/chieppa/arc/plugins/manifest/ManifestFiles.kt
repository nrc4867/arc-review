package dev.chieppa.arc.plugins.manifest

/** The file names a plugin folder and a folder of plugins are known by. */
object ManifestFiles {
    /** One plugin's manifest, at its folder's root. */
    const val PLUGIN = "plugin.json"

    /** A folder holding several plugins, listing their folders. */
    const val PLUGINS = "plugins.json"
}
