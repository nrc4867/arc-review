package dev.chieppa.arc.plugins.notes

import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.NoteCreateParams
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NotesListParams
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException

/**
 * The person's notes as plugins reach them, kept in Arc's own conversation. A plugin reads notes and adds notes or
 * comment drafts; a draft stays on this machine until the person stages and pushes it, which no plugin can do.
 */
interface PluginNotes {
    /** The person's notes [params] picks, oldest first. */
    suspend fun list(params: NotesListParams): List<NoteInfo>

    /**
     * Adds a note, or a comment draft when [kind] is [NoteKind.Draft], as [params] says. Throws [RpcException] when its
     * source cannot be read.
     */
    suspend fun add(kind: NoteKind, params: NoteCreateParams): NoteInfo

    companion object {
        private const val NO_NOTES = "No notes here"

        /** No window: no notes, and none can be added. */
        val NONE = object : PluginNotes {
            override suspend fun list(params: NotesListParams): List<NoteInfo> = emptyList()

            override suspend fun add(kind: NoteKind, params: NoteCreateParams): NoteInfo = throw RpcException(RpcError(ErrorCodes.FAILED, NO_NOTES))
        }
    }
}
