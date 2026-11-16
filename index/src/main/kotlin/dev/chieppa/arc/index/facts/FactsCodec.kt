package dev.chieppa.arc.index.facts

import dev.chieppa.arc.syntax.facts.Definition
import dev.chieppa.arc.syntax.facts.FileFacts
import dev.chieppa.arc.syntax.facts.RefKind
import dev.chieppa.arc.syntax.facts.Reference
import dev.chieppa.arc.syntax.facts.SymbolKind
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/** [FileFacts] as bytes, for [DiskFactsStore]. */
object FactsCodec {
    /** The format's mark and version: `AF` and 1. */
    private const val MAGIC = 0x41460001
    private const val ABSENT = -1

    fun encode(facts: FileFacts): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(MAGIC)
            out.text(facts.grammar)
            out.writeInt(facts.chars)
            out.writeInt(facts.lineCount)
            out.text(facts.packageName)
            out.texts(facts.imports)
            out.writeInt(facts.definitions.size)
            facts.definitions.forEach { out.definition(it) }
            out.writeInt(facts.references.size)
            facts.references.forEach { out.reference(it) }
            out.writeInt(facts.errorChars)
        }
        return bytes.toByteArray()
    }

    /** The facts in [bytes]; throws [java.io.IOException] for bytes in another format. */
    fun decode(bytes: ByteArray): FileFacts = DataInputStream(ByteArrayInputStream(bytes)).use { input ->
        if (input.readInt() != MAGIC) throw java.io.IOException("Not facts of this format")
        FileFacts(
            grammar = input.text()!!,
            chars = input.readInt(),
            lineCount = input.readInt(),
            packageName = input.text(),
            imports = input.texts()!!,
            definitions = List(input.readInt()) { input.definition() },
            references = List(input.readInt()) { input.reference() },
            errorChars = input.readInt(),
        )
    }

    private fun DataOutputStream.definition(d: Definition) {
        text(d.name)
        text(d.label)
        writeInt(d.kind.ordinal)
        for (value in intArrayOf(d.nameStart, d.nameEnd, d.line, d.column, d.spanStart, d.spanEnd, d.parent)) writeInt(value)
        texts(d.parameters)
        text(d.type)
        text(d.container)
        writeInt(d.containerKind?.ordinal ?: ABSENT)
        texts(d.supertypes)
    }

    private fun DataInputStream.definition(): Definition {
        val name = text()!!
        val label = text()!!
        val kind = SymbolKind.entries[readInt()]
        val ints = IntArray(DEFINITION_INTS) { readInt() }
        return Definition(
            name = name, label = label, kind = kind,
            nameStart = ints[0], nameEnd = ints[1], line = ints[2], column = ints[3], spanStart = ints[4], spanEnd = ints[5], parent = ints[6],
            parameters = texts(), type = text(), container = text(),
            containerKind = readInt().takeIf { it != ABSENT }?.let { SymbolKind.entries[it] },
            supertypes = texts()!!,
        )
    }

    private fun DataOutputStream.reference(r: Reference) {
        text(r.name)
        writeInt(r.kind.ordinal)
        for (value in intArrayOf(r.start, r.end, r.line, r.column, r.arity ?: ABSENT)) writeInt(value)
        writeBoolean(r.qualified)
        text(r.schema)
    }

    private fun DataInputStream.reference(): Reference {
        val name = text()!!
        val kind = RefKind.entries[readInt()]
        val ints = IntArray(REFERENCE_INTS) { readInt() }
        return Reference(
            name = name, kind = kind, start = ints[0], end = ints[1], line = ints[2], column = ints[3],
            arity = ints[4].takeIf { it != ABSENT }, qualified = readBoolean(), schema = text(),
        )
    }

    private fun DataOutputStream.text(value: String?) {
        if (value == null) return writeInt(ABSENT)
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeInt(bytes.size)
        write(bytes)
    }

    private fun DataInputStream.text(): String? {
        val size = readInt()
        if (size == ABSENT) return null
        return String(ByteArray(size).also(::readFully), Charsets.UTF_8)
    }

    private fun DataOutputStream.texts(values: List<String>?) {
        writeInt(values?.size ?: ABSENT)
        values?.forEach { text(it) }
    }

    private fun DataInputStream.texts(): List<String>? {
        val size = readInt()
        if (size == ABSENT) return null
        return List(size) { text()!! }
    }

    private const val DEFINITION_INTS = 7
    private const val REFERENCE_INTS = 5
}
