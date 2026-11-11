package dev.chieppa.arc.ui.plugins.notes

import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.plugins.notes.PluginNotes
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.NoteCreateParams
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NoteSide
import dev.chieppa.arc.plugins.protocol.NotesListParams
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.withContext

/**
 * The window's notes for plugins: read from and added to the conversation the window keeps, on the window's main
 * dispatcher, so a plugin's note shows in the Notes drawer and a draft in its pane like the person's own.
 */
class WindowNotes(private val workbench: Workbench) : PluginNotes {
    private val controller get() = workbench.comments.controller

    override suspend fun list(params: NotesListParams): List<NoteInfo> = withContext(workbench.dispatchers.main) {
        val source = params.source?.let(::sourceOf)
        controller.local.comments
            .filter { it.isNote && (source == null || it.source == source) && (params.path == null || it.anchor?.path == params.path) }
            .map { PluginNoteInfos.of(workbench, it) }
    }

    override suspend fun add(kind: NoteKind, params: NoteCreateParams): NoteInfo = withContext(workbench.dispatchers.main) {
        val source = sourceOf(params.source ?: params.context.source?.ref ?: throw failed(NO_SOURCE))
        val anchor = params.line?.let { line ->
            val head = workbench.state.loads[source]?.ready?.revisions?.head
            CommentAnchor(params.path.orEmpty(), line, PluginNoteInfos.sideOf(params.side ?: NoteSide.New), params.startLine, head)
        }
        val added = when (kind) {
            NoteKind.Note -> controller.addNote(source, params.text, anchor)
            NoteKind.Draft -> controller.addDraft(source, params.text, anchor)
        }
        PluginNoteInfos.of(workbench, added)
    }

    private fun sourceOf(ref: String): Source = SourceRefs.parse(ref) ?: throw failed("$BAD_SOURCE $ref")

    private fun failed(message: String) = RpcException(RpcError(ErrorCodes.FAILED, message))

    private companion object {
        const val NO_SOURCE = "No source to add it to"
        const val BAD_SOURCE = "Not a source:"
    }
}
