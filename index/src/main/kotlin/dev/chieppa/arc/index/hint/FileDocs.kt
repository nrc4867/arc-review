package dev.chieppa.arc.index.hint

import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HintDoc
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.syntax.docs.DocComments

/** The doc comments of a file, read, for drawing them in place: one per definition that has one, top to bottom. */
class FileDocs(private val index: WorkspaceIndex) {
    /** The doc comments of [document], whose content is [text]; empty when the index cannot read it. */
    suspend fun of(document: DocumentRef, text: String): List<HintDoc> {
        val file = index.file(RepoName(document.repo), document.revision, document.path) ?: return emptyList()
        return file.facts.definitions.asSequence()
            .filter { !it.isLocal }
            .mapNotNull { DocComments.before(file.grammar, text, it.spanStart) }
            .distinctBy { it.firstLine }
            .map(HintDocs::of)
            .sortedBy { it.firstLine }
            .toList()
    }
}
