package dev.chieppa.arc.syntax.docs

import dev.chieppa.arc.syntax.grammar.Grammar
import dev.chieppa.arc.syntax.grammar.Grammars
import dev.chieppa.arc.syntax.grammar.LanguageFamily

/** Finds the doc comment above a definition and reads it in its grammar's convention. */
object DocComments {
    /** The doc comment written immediately before index [offset] of [text], or null when there is none. */
    fun before(grammar: Grammar, text: String, offset: Int): DocComment? {
        if (offset < 0 || offset > text.length) return null
        return when (grammar.family) {
            LanguageFamily.Jvm -> DocCommentLocator.docBlock(text, offset, skipKeywords = false)?.let {
                if (grammar.id == Grammars.java.id) JavaDocParser.parse(it.text, it.firstLine) else KDocParser.parse(it.text, it.firstLine)
            }
            LanguageFamily.JavaScript -> DocCommentLocator.docBlock(text, offset, skipKeywords = true)?.let {
                JsDocParser.parse(it.text, it.firstLine, typescript = grammar.id != Grammars.javascript.id)
            }
            LanguageFamily.Sql -> DocCommentLocator.sqlComment(text, offset)?.let { SqlCommentParser.parse(it.text, it.firstLine) }
        }
    }
}
