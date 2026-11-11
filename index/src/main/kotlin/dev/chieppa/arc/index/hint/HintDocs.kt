package dev.chieppa.arc.index.hint

import dev.chieppa.arc.codeintel.model.HintDoc
import dev.chieppa.arc.codeintel.model.HintEntry
import dev.chieppa.arc.codeintel.model.HintSection
import dev.chieppa.arc.codeintel.model.HintSectionKind
import dev.chieppa.arc.syntax.docs.DocComment
import dev.chieppa.arc.syntax.docs.DocFormat
import dev.chieppa.arc.syntax.docs.DocTagKind

/** A read doc comment as a hint carries it: its tags gathered by kind, in the order each kind first appears. */
internal object HintDocs {
    fun of(doc: DocComment): HintDoc {
        val sections = LinkedHashMap<Pair<HintSectionKind, String?>, MutableList<HintEntry>>()
        for (tag in doc.tags) {
            val kind = kindOf(tag.kind)
            val other = kind == HintSectionKind.Other
            sections.getOrPut(kind to tag.name.takeIf { other }) { ArrayList() } +=
                HintEntry(tag.text, tag.line, tag.name.takeUnless { other }, tag.type)
        }
        return HintDoc(
            format = formatOf(doc.format),
            description = doc.description,
            firstLine = doc.firstLine,
            lastLine = doc.lastLine,
            deprecated = doc.deprecated,
            sections = sections.map { (key, entries) -> HintSection(key.first, entries, key.second) },
        )
    }

    private fun kindOf(kind: DocTagKind) = when (kind) {
        DocTagKind.Param -> HintSectionKind.Parameters
        DocTagKind.Property -> HintSectionKind.Properties
        DocTagKind.TypeParam -> HintSectionKind.TypeParameters
        DocTagKind.Returns -> HintSectionKind.Returns
        DocTagKind.Throws -> HintSectionKind.Throws
        DocTagKind.See -> HintSectionKind.See
        DocTagKind.Since -> HintSectionKind.Since
        DocTagKind.Example -> HintSectionKind.Examples
        DocTagKind.Author -> HintSectionKind.Authors
        DocTagKind.Other -> HintSectionKind.Other
    }

    private fun formatOf(format: DocFormat) = when (format) {
        DocFormat.JavaDoc -> HintFormats.JAVADOC
        DocFormat.KDoc -> HintFormats.KDOC
        DocFormat.JsDoc -> HintFormats.JSDOC
        DocFormat.SqlComment -> HintFormats.SQL
    }
}
