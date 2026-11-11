package dev.chieppa.arc.plugins.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind

/** Writes a protocol type's serial descriptor as a TypeScript interface, so the data plugins get matches what Arc sends. */
@OptIn(ExperimentalSerializationApi::class)
object DescriptorTypes {
    /** The interface for [descriptor], named by its class's simple name, with its fields' types. */
    fun interfaceOf(descriptor: SerialDescriptor): String = buildString {
        append("export interface ${nameOf(descriptor)} {\n")
        fieldsOf(descriptor).forEach { append("    readonly ${it.name}${if (it.optional) "?" else ""}: ${it.type.ts};\n") }
        append("}\n")
    }

    /** Each field of [descriptor] with its TypeScript type, optional where it may be null. */
    fun fieldsOf(descriptor: SerialDescriptor): List<PropSpec> = (0 until descriptor.elementsCount).map { index ->
        val element = descriptor.getElementDescriptor(index)
        PropSpec(descriptor.getElementName(index), PropType.Named(typeOf(element)), "", element.isNullable)
    }

    /** The union of an enum's serial names. */
    fun unionOf(descriptor: SerialDescriptor): String =
        (0 until descriptor.elementsCount).joinToString(" | ") { "\"${descriptor.getElementName(it)}\"" }

    fun nameOf(descriptor: SerialDescriptor): String = descriptor.serialName.removeSuffix("?").substringAfterLast('.')

    private fun typeOf(descriptor: SerialDescriptor): String {
        val base = when (val kind: SerialKind = descriptor.kind) {
            PrimitiveKind.STRING, PrimitiveKind.CHAR -> "string"
            PrimitiveKind.BOOLEAN -> "boolean"
            is PrimitiveKind -> "number"
            StructureKind.LIST -> "readonly ${typeOf(descriptor.getElementDescriptor(0))}[]"
            StructureKind.MAP -> "Readonly<Record<string, ${typeOf(descriptor.getElementDescriptor(1))}>>"
            else -> nameOf(descriptor)
        }
        return base
    }
}
