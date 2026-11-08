package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.syntax.grammar.Grammars
import dev.chieppa.arc.syntax.highlight.Highlighter
import dev.chieppa.arc.syntax.highlight.LineSpans
import dev.chieppa.arc.syntax.natives.NativeLibraries
import dev.chieppa.stretch.code.CodeToken
import dev.chieppa.stretch.code.TokenKind

/**
 * Highlighting from tree-sitter for the viewers: each line's tokens, offsets from the line's start. Null where tree-sitter
 * cannot colour the file (no grammar for it, no libraries for this machine, or a SQL file it reads too badly), so the
 * viewer falls back to its own tokenizer.
 */
object SyntaxTokens {
    /** Whether tree-sitter reads files at [path] on this machine. */
    fun reads(path: String): Boolean = Grammars.forPath(path) != null && NativeLibraries.available

    fun lines(path: String, text: String): List<List<CodeToken>>? {
        val grammar = Grammars.forPath(path) ?: return null
        if (!NativeLibraries.available) return null
        val spans = try {
            Highlighter.highlight(grammar, text)
        } catch (_: LinkageError) {
            null
        } ?: return null
        return LineSpans.split(text, spans).map { line -> line.map { CodeToken(it.start, it.end, TokenKind.valueOf(it.style.name)) } }
    }
}
