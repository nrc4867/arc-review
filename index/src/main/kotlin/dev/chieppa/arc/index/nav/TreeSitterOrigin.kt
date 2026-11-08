package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.AnswerOrigin
import dev.chieppa.arc.codeintel.model.DocumentSelector
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.arc.syntax.grammar.Grammars

/** How the tree-sitter index signs its answers, and the files it answers for. */
object TreeSitterOrigin {
    const val PROVIDER = "tree-sitter"

    val selector = DocumentSelector(Grammars.all.map { it.language }.toSet())

    fun of(language: String?) = AnswerOrigin(PROVIDER, Precision.ByName, language)
}
