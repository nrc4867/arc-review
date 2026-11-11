package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.manifest.ApiVersion
import dev.chieppa.arc.plugins.protocol.FileInfo
import dev.chieppa.arc.plugins.protocol.GrepHit
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NoteSide
import dev.chieppa.arc.plugins.protocol.RepoInfo
import dev.chieppa.arc.plugins.protocol.SettingType
import dev.chieppa.arc.plugins.protocol.SourceInfo

/**
 * Writes `arc.d.ts`, the TypeScript types of the plugin API, from Arc's Kotlin definitions: the protocol's data
 * types, the components' props and the `app` object's interfaces. The SDK and every plugin are written against it,
 * so the types and Arc cannot drift; a test fails while the committed file differs from what this writes.
 */
object TypesGenerator {
    /** Where the committed file lives, relative to the repo's root. */
    const val PATH = "plugin-sdk/src/generated/arc.d.ts"

    fun generate(): String = buildString {
        append(HEADER)
        append("export type ApiVersion = ${ApiVersion.CURRENT};\n\n")
        append("export type IconName = ${PluginIcons.NAMES.joinToString(" | ") { "\"$it\"" }};\n\n")
        append("export type AppEventName = ${AppApi.eventNames.joinToString(" | ") { "\"$it\"" }};\n\n")
        append("export type SettingType = ${DescriptorTypes.unionOf(SettingType.serializer().descriptor)};\n\n")
        listOf(NoteKind.serializer().descriptor, NoteSide.serializer().descriptor).forEach {
            append("export type ${DescriptorTypes.nameOf(it)} = ${DescriptorTypes.unionOf(it)};\n\n")
        }
        listOf(RepoInfo.serializer(), SourceInfo.serializer(), FileInfo.serializer(), GrepHit.serializer(), NoteInfo.serializer()).forEach {
            append(DescriptorTypes.interfaceOf(it.descriptor)).append('\n')
        }
        append(SETTINGS)
        Components.all.forEach { append(propsOf(it)).append('\n') }
        AppApi.interfaces.forEach { append(interfaceOf(it)).append('\n') }
    }.trimEnd() + "\n"

    private fun propsOf(component: ComponentSpec): String = buildString {
        append("/** ${component.doc} */\n")
        val generic = component.generic?.let { "<$it>" }.orEmpty()
        append("export interface ${component.propsName}$generic {\n")
        component.props.forEach { append(member(it)) }
        append("}\n")
    }

    private fun interfaceOf(spec: InterfaceSpec): String = buildString {
        append("/** ${spec.doc} */\n")
        append("export interface ${spec.name} {\n")
        spec.members.forEach { append(member(it)) }
        append("}\n")
    }

    private fun member(prop: PropSpec): String =
        "    /** ${prop.doc} */\n    readonly ${prop.name}${if (prop.optional) "?" else ""}: ${prop.type.ts};\n"

    private const val HEADER = "// Generated from Arc's Kotlin definitions by ./gradlew :plugins:generatePluginTypes. Do not edit.\n\n" +
        "import type { ReactNode } from \"react\";\n\n"

    private const val SETTINGS = """/** A setting a plugin declares in definePlugin. */
export interface SettingSpec {
    readonly type: SettingType;
    readonly default?: unknown;
    readonly label?: string;
    readonly of?: SettingType;
    readonly options?: readonly string[];
}

type ScalarValue<T> = T extends "number" ? number : T extends "boolean" ? boolean : string;

/** The value a setting of spec T holds. */
export type SettingValue<T extends SettingSpec> = T["type"] extends "list"
    ? ScalarValue<T["of"]>[]
    : T["type"] extends "map"
      ? Record<string, ScalarValue<T["of"]>>
      : ScalarValue<T["type"]>;

/** The values of the settings D declares. */
export type SettingsOf<D> = { [K in keyof D]: D[K] extends SettingSpec ? SettingValue<D[K]> : never };

"""
}
