package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.CommentsAccess
import dev.chieppa.arc.plugins.manifest.NotesAccess
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.AppEvents
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.NoteCreateParams
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NoteSide
import dev.chieppa.arc.plugins.protocol.NotesListParams
import dev.chieppa.arc.plugins.protocol.NotesListResult
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.RpcException
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A plugin reads and adds the person's notes, and starts drafts, only as its manifest lets it. */
@OptIn(ExperimentalTestApi::class)
class PluginNotesUiTest {
    private val rig = PluginRig(
        """{"notes":{"read":true,"write":true},"comments":{"draft":true}}""",
        Contributions(),
        Capabilities(notes = NotesAccess(read = true, write = true), comments = CommentsAccess(draft = true)),
    ) { "[]" }

    @AfterTest
    fun cleanUp() = rig.cleanUp()

    private fun <T> call(workbench: Workbench, method: String, params: JsonElement, answer: KSerializer<T>): T = runBlocking {
        ProtocolJson.fromElement(answer, workbench.plugins.manager!!.instance(PluginRig.ID)!!.request(method, params))
    }

    private fun ComposeUiTest.add(workbench: Workbench, method: String, params: NoteCreateParams): NoteInfo =
        call(workbench, method, ProtocolJson.toElement(NoteCreateParams.serializer(), params), NoteInfo.serializer()).also { waitForIdle() }

    @Test
    fun aNoteAPluginAddsIsThePersonsOwnAndItReadsItBack() = rig.test {
        val workbench = rig.open(this)
        val note = add(workbench, Methods.NOTES_CREATE, NoteCreateParams("Check the retry", "api#412", "src/a.kt", 14, 12, NoteSide.Old))
        val kept = workbench.comments.controller.local.byId(note.id)!!
        assertEquals(LocalKind.Note, kept.kind)
        assertEquals(LineSide.Old, kept.anchor?.side)
        assertEquals(12, kept.anchor?.startLine)
        assertEquals(NoteKind.Note, note.kind)
        assertEquals("api", note.repo.name)
        assertEquals(kept.writtenAt * 1000, note.createdAt)
        val listed = call(workbench, Methods.NOTES_LIST, ProtocolJson.toElement(NotesListParams.serializer(), NotesListParams("api#412", "src/a.kt")), NotesListResult.serializer())
        assertEquals(listOf(note), listed.notes)
        val elsewhere = call(workbench, Methods.NOTES_LIST, ProtocolJson.toElement(NotesListParams.serializer(), NotesListParams(path = "src/b.kt")), NotesListResult.serializer())
        assertEquals(emptyList(), elsewhere.notes)
    }

    @Test
    fun aDraftStaysADraftAndIsNotListedAsANote() = rig.test {
        val workbench = rig.open(this)
        val draft = add(workbench, Methods.COMMENTS_DRAFT, NoteCreateParams("Rename this?", "api#412", "src/a.kt", 3))
        assertEquals(LocalKind.Draft, workbench.comments.controller.local.byId(draft.id)?.kind)
        assertEquals(NoteKind.Draft, draft.kind)
        val listed = call(workbench, Methods.NOTES_LIST, ProtocolJson.toElement(NotesListParams.serializer(), NotesListParams()), NotesListResult.serializer())
        assertEquals(emptyList(), listed.notes)
    }

    @Test
    fun itHearsWhenTheNotesOnASourceChange() = rig.test {
        val workbench = rig.open(this)
        val note = add(workbench, Methods.NOTES_CREATE, NoteCreateParams("Later", "api#412"))
        val event = rig.process.received.filterIsInstance<Envelope.Notification>().filter { it.method == Methods.EVENT }
            .map { ProtocolJson.fromElement(AppEventParams.serializer(), it.params) }
            .single { it.name == AppEvents.NOTES_CHANGED }
        assertEquals("api#412", event.context.source?.ref)
        assertTrue(note.id in event.data.getValue(AppEvents.IDS).toString())
    }

    @Test
    fun withoutTheCapabilityItCannotReadNotes() {
        val bare = PluginRig("""{"panels":true}""", Contributions(), Capabilities(panels = true)) { "[]" }
        try {
            bare.test {
                val workbench = bare.open(this)
                val refused = assertFailsWith<RpcException> {
                    call(workbench, Methods.NOTES_LIST, ProtocolJson.toElement(NotesListParams.serializer(), NotesListParams()), NotesListResult.serializer())
                }
                assertEquals(ErrorCodes.DENIED, refused.error.code)
                workbench.comments.controller.addNote(SourceRefs.parse("api#412")!!, "Mine")
                waitForIdle()
                assertNull(bare.process.received.filterIsInstance<Envelope.Notification>().firstOrNull { it.method == Methods.EVENT })
            }
        } finally {
            bare.cleanUp()
        }
    }
}
