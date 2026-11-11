package dev.chieppa.arc.plugins.starter

/** What a starter plugin's folder holds, by path in it. */
object StarterFiles {
    const val MANIFEST = "plugin.json"
    const val TSCONFIG = "tsconfig.json"
    const val ENTRY = "src/index.tsx"
    const val GUIDE = "CLAUDE.md"

    /** The folder Arc's generated types and reference are copied to. */
    const val ARC = "arc"

    /** Each written file and the template resource it comes from. */
    val TEMPLATES: Map<String, String> = mapOf(
        MANIFEST to "plugin.json.template",
        TSCONFIG to "tsconfig.json.template",
        ENTRY to "index.tsx.template",
        GUIDE to "CLAUDE.md.template",
    )

    /** The host's folders copied into [ARC]: the SDK's types and the reference. */
    val COPIED: List<String> = listOf("types/", "docs/")

    /** The panel and action the starter's entry declares. */
    const val PANEL = "panel"
    const val ACTION = "hello"
}
