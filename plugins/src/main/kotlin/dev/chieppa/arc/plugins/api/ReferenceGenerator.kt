package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.manifest.ApiVersion

/**
 * Writes `reference.md`, the plugin API's reference in Markdown, from the same Kotlin definitions as `arc.d.ts`:
 * `plugin.json` and its capabilities, definePlugin, every component's props, the `app` object, the events, the data
 * types and the icon names. A starter plugin carries a copy, so its author reads what this Arc offers; a test fails
 * while the committed file differs from what this writes.
 */
object ReferenceGenerator {
    /** Where the committed file lives, relative to the repo's root. */
    const val PATH = "plugin-sdk/src/generated/reference.md"

    fun generate(): String = buildString {
        append("# Arc plugin API reference\n\n")
        append("Plugin API version ${ApiVersion.CURRENT}. Generated from Arc's Kotlin definitions by ${TypesGenerator.COMMAND}; ")
        append("the types in `arc.d.ts` come from the same definitions. Do not edit.\n\n")
        append("## plugin.json\n\n")
        append(table(listOf(KEY, TYPE, WHAT), ManifestApi.fields.map { listOf(code(it.name), code(it.type), it.doc) }))
        append("\n### Capabilities\n\n")
        append(CAPABILITIES_NOTE)
        append(table(listOf(KEY, TYPE, WHAT), ManifestApi.capabilities.map { listOf(code(it.name), code(it.type), it.doc) }))
        append(DEFINE_PLUGIN)
        append("## Components\n\n")
        append(COMPONENTS_NOTE)
        Components.all.forEach { component ->
            append("### ${component.name}\n\n${component.doc}\n\n")
            append(table(listOf(PROP, TYPE, WHAT), component.props.map { listOf(code(it.name + if (it.optional) "?" else ""), code(it.type.ts), it.doc) }))
            append('\n')
        }
        append("## The app object\n\n")
        AppApi.interfaces.forEach { spec ->
            append("### ${spec.name.substringBefore('<')}\n\n${spec.doc}\n\n")
            append(table(listOf(MEMBER, TYPE, WHAT), spec.members.map { listOf(code(it.name + if (it.optional) "?" else ""), code(it.type.ts), it.doc) }))
            append('\n')
        }
        append("## Events\n\n")
        append(table(listOf(NAME, WHAT), AppApi.events.map { listOf(code(it.name), it.doc) }))
        append("\n## Data\n\n")
        DataTypes.unions.forEach { append("- `${DescriptorTypes.nameOf(it)}`: ${code(DescriptorTypes.unionOf(it))}\n") }
        append('\n')
        DataTypes.interfaces.forEach { descriptor ->
            append("### ${DescriptorTypes.nameOf(descriptor)}\n\n")
            append(table(listOf(FIELD, TYPE), DescriptorTypes.fieldsOf(descriptor).map { listOf(code(it.name + if (it.optional) "?" else ""), code(it.type.ts)) }))
            append('\n')
        }
        append("## Icons\n\n")
        append(PluginIcons.NAMES.joinToString(", ") { code(it) }).append('\n')
    }

    private fun table(headings: List<String>, rows: List<List<String>>): String = buildString {
        append(headings.joinToString(" | ", "| ", " |\n"))
        append(headings.joinToString(" | ", "| ", " |\n") { "---" })
        rows.forEach { append(it.joinToString(" | ", "| ", " |\n") { cell -> cell.replace("|", "\\|") }) }
    }

    private fun code(text: String): String = "`$text`"

    private const val KEY = "Key"
    private const val TYPE = "Type"
    private const val WHAT = "What"
    private const val PROP = "Prop"
    private const val MEMBER = "Member"
    private const val NAME = "Name"
    private const val FIELD = "Field"

    private const val CAPABILITIES_NOTE = "A plugin declares in `capabilities` everything it may do. Arc checks every call " +
        "against them, drops contributions of kinds it does not declare, and a later version that asks for more stays " +
        "off until the person enables it again.\n\n"

    private const val COMPONENTS_NOTE = "Import components from `@review/plugin`. Arc draws each with its own Stretch " +
        "composable, so there is no CSS and no DOM. `List` takes `items` and a function drawing each as a `List.Row`; " +
        "`Console` takes the handle `app.console.run` gives, or its id.\n\n"

    private const val DEFINE_PLUGIN = """
## definePlugin

The entry file's default export is `definePlugin({ ... })` from `@review/plugin`, with any of:

| Key | What |
| --- | --- |
| `settings` | The settings it keeps, by key: `{ type, default, label, of, options }`; `app.settings` holds their values. |
| `settingsPage` | `(app) => ReactNode`: its own page under Settings › Plugins. |
| `actions` | `{ id, label, keys?, icon?, run(app, context) }[]`: commands in the palette, menus and keys. Needs `actions`. |
| `panels` | `{ id, title, icon?, render(app) }[]`: drawers with their own rail icons. Needs `panels`. |
| `panes` | `{ id, title, icon?, render(app) }[]`: pane kinds in the grid; `app.props` holds what one was opened with. Needs `panes`. |
| `on` | `{ [event]: (app, event) => void }`: what to do when something happens in Arc. |

A component that takes the app object types it as `App` (or `App<Settings>` to read typed settings).

"""
}
