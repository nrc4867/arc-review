package dev.chieppa.arc.plugins.notes

import dev.chieppa.arc.plugins.manager.PluginUi
import dev.chieppa.arc.plugins.protocol.NoteCreateParams
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NotesListParams
import dev.chieppa.arc.plugins.protocol.PanesOpenParams
import dev.chieppa.arc.plugins.protocol.RepoInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** The window's notes played by a test, kept in [all], as a [PluginUi] whose panes open nowhere. */
class FakeNotes : PluginNotes, PluginUi {
    private val kept = MutableStateFlow<List<NoteInfo>>(emptyList())

    val all: StateFlow<List<NoteInfo>> get() = kept

    override val notes: PluginNotes get() = this

    override suspend fun openPane(plugin: String, params: PanesOpenParams) = Unit

    override suspend fun list(params: NotesListParams): List<NoteInfo> = kept.value.filter {
        it.kind == NoteKind.Note && (params.source == null || it.source == params.source) && (params.path == null || it.path == params.path)
    }

    override suspend fun add(kind: NoteKind, params: NoteCreateParams): NoteInfo {
        val source = params.source ?: params.context.source?.ref.orEmpty()
        val note = NoteInfo(
            "n${kept.value.size + 1}", kind, source, RepoInfo(source.substringBefore('#'), source.substringBefore('#')), params.text, CREATED, CREATED,
            params.path, params.line, params.startLine, params.side,
        )
        synchronized(this) { kept.value = kept.value + note }
        return note
    }

    private companion object {
        const val CREATED = 1_700_000_000_000L
    }
}
