package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.notes.PluginNotes
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.NoteCreateParams
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NotesListParams
import dev.chieppa.arc.plugins.protocol.NotesListResult
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException

/** `app.notes` and `app.comments` for one plugin, through the window's [notes]; capabilities are checked before. */
internal class NoteCalls(private val notes: () -> PluginNotes) {
    suspend fun list(params: NotesListParams): NotesListResult = NotesListResult(notes().list(params))

    suspend fun add(kind: NoteKind, params: NoteCreateParams): NoteInfo {
        if (params.text.isBlank()) throw RpcException(RpcError(ErrorCodes.BAD_PARAMS, EMPTY))
        if (params.line == null && (params.startLine != null || params.side != null)) throw RpcException(RpcError(ErrorCodes.BAD_PARAMS, NO_LINE))
        if (params.line != null && params.path == null) throw RpcException(RpcError(ErrorCodes.BAD_PARAMS, NO_PATH))
        if (params.line != null && (params.line < 1 || (params.startLine != null && params.startLine !in 1..params.line))) {
            throw RpcException(RpcError(ErrorCodes.BAD_PARAMS, BAD_RANGE))
        }
        return notes().add(kind, params)
    }

    private companion object {
        const val EMPTY = "A note needs text"
        const val NO_LINE = "A range or side needs a line"
        const val NO_PATH = "A line needs a path"
        const val BAD_RANGE = "Lines count from 1, and a range starts at or before its line"
    }
}
