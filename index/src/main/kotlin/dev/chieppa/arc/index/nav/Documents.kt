package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.CodeLocation
import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.syntax.grammar.Grammars

/** Document references for files: the language a path parses as, filled in. */
object Documents {
    fun of(repo: RepoName, revision: String, path: String) = DocumentRef(repo.value, revision, path, Grammars.forPath(path)?.language)

    fun of(file: IndexedFile) = DocumentRef(file.repo.value, file.revision, file.path, file.grammar.language)

    fun location(file: IndexedFile, line: Int, column: Int, length: Int) = CodeLocation(of(file), CodeRange.onLine(line, column, length))
}
