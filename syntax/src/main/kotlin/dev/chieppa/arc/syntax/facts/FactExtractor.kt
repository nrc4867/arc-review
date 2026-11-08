package dev.chieppa.arc.syntax.facts

import dev.chieppa.arc.syntax.grammar.Grammar
import dev.chieppa.arc.syntax.runtime.SyntaxTree

/** Parses a file's text and reads its [FileFacts]. Every query runs natively, once per file. */
object FactExtractor {
    fun extract(grammar: Grammar, text: String): FileFacts = SyntaxTree.parse(grammar, text).use { extract(it) }

    fun extract(tree: SyntaxTree): FileFacts {
        val lines = LineIndex(tree.text)
        val structure = FileStructure.read(tree)
        val definitions = DefinitionCollector.collect(tree, lines)
        val references = ReferenceCollector.collect(tree, lines, definitions, structure.sqlNames)
        return FileFacts(
            grammar = tree.grammar.id,
            chars = tree.text.length,
            lineCount = lines.lineCount,
            packageName = structure.packageName,
            imports = structure.imports,
            definitions = definitions,
            references = references,
            errorChars = ParseErrors.chars(tree),
        )
    }
}
