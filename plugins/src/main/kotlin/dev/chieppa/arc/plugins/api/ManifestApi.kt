package dev.chieppa.arc.plugins.api

/**
 * The keys of `plugin.json` and of its capabilities, as the reference documents them. A test holds them to
 * [dev.chieppa.arc.plugins.manifest.PluginManifest]'s and [dev.chieppa.arc.plugins.manifest.Capabilities]' fields.
 */
object ManifestApi {
    val fields: List<FieldDoc> = listOf(
        FieldDoc("id", "string", "The plugin's id: lowercase letters, digits, dots, dashes and underscores, at most 64."),
        FieldDoc("version", "string", "Its version, shown in Settings › Plugins."),
        FieldDoc("apiVersion", "number", "The plugin API version it is written for."),
        FieldDoc("entry", "string", "The TypeScript or JavaScript file whose default export is definePlugin(...), relative to its folder."),
        FieldDoc("name", "string", "The name shown; the id when absent."),
        FieldDoc("description", "string", "One line on what it does."),
        FieldDoc("icon", "IconName", "Its icon in Settings › Plugins."),
        FieldDoc("capabilities", "object", "What it may do, shown to the person before any of its code runs; see Capabilities."),
    )

    val capabilities: List<FieldDoc> = listOf(
        FieldDoc("panels", "boolean", "Adds drawers with their own rail icons: definePlugin's panels."),
        FieldDoc("panes", "boolean", "Adds pane kinds to the grid, definePlugin's panes, and opens them with app.panes.open."),
        FieldDoc("actions", "boolean", "Adds commands to the palette, menus and keys: definePlugin's actions."),
        FieldDoc("console", "boolean", "Runs programs through app.console.run; Arc runs them and a Console shows them."),
        FieldDoc("git", "boolean", "Queries the open sources' repos through app.git."),
        FieldDoc("notes", "{ read?: boolean; write?: boolean }", "app.notes: read lists the person's notes and hears notes.changed; write adds notes."),
        FieldDoc("comments", "{ draft?: boolean }", "app.comments.draft starts a comment draft, which only the person stages and pushes."),
        FieldDoc("files", "{ read?: string[]; write?: string[] }", "The folders app.files may read and write, relative to the plugin's folder or absolute."),
        FieldDoc("network", "string[]", "The hosts app.fetch may reach; *.host names its subdomains."),
        FieldDoc("mcp", "string[]", "The MCP server roles the plugin calls."),
    )
}
