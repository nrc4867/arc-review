package dev.chieppa.arc.ui.plugins.notes

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NoteSide
import dev.chieppa.arc.ui.plugins.PluginContexts
import dev.chieppa.arc.ui.workbench.Workbench

/** The person's notes and drafts as plugins see them. */
object PluginNoteInfos {
    private const val MILLIS = 1000L

    /** [comment] as a plugin reads it, its repo named as [PluginContexts] names it. */
    fun of(workbench: Workbench, comment: LocalComment): NoteInfo {
        val anchor = comment.anchor
        return NoteInfo(
            id = comment.id,
            kind = if (comment.kind == LocalKind.Note) NoteKind.Note else NoteKind.Draft,
            source = comment.source.ref,
            repo = PluginContexts.info(workbench, comment.source).repo,
            text = comment.body,
            createdAt = comment.writtenAt * MILLIS,
            updatedAt = comment.updatedAt * MILLIS,
            path = anchor?.path,
            line = anchor?.line,
            startLine = anchor?.startLine,
            side = anchor?.side?.let(::sideOf),
            revision = anchor?.madeOn?.value,
        )
    }

    fun sideOf(side: LineSide): NoteSide = if (side == LineSide.Old) NoteSide.Old else NoteSide.New

    fun sideOf(side: NoteSide): LineSide = if (side == NoteSide.Old) LineSide.Old else LineSide.New
}
