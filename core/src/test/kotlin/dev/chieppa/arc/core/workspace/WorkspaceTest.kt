package dev.chieppa.arc.core.workspace

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.source.SourceRefs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class WorkspaceTest {
    private val prA = SourceRefs.parse("api#412")!!
    private val prB = SourceRefs.parse("web#88")!!
    private val commit = SourceRefs.parse("web@3fa9c1e")!!
    private val empty = Workspace()

    @Test
    fun theFirstSourceAddedBecomesActive() {
        val workspace = empty.add(prA).add(prB)
        assertEquals(listOf(prA, prB), workspace.sources)
        assertEquals(prA, workspace.active)
    }

    @Test
    fun addingASourceTwiceKeepsOne() {
        assertEquals(listOf(prA), empty.add(prA).add(prA).sources)
    }

    @Test
    fun activatingOnlyTakesTheWorkspacesOwnSources() {
        val workspace = empty.add(prA).add(prB)
        assertEquals(prB, workspace.activate(prB).active)
        assertEquals(prA, workspace.activate(commit).active)
    }

    @Test
    fun removingTheActiveSourceActivatesTheNextOrElseThePrevious() {
        val workspace = empty.add(prA).add(prB).add(commit).activate(prB)
        assertEquals(commit, workspace.remove(prB).active)
        assertEquals(prB, workspace.activate(commit).remove(commit).active)
        assertEquals(prB, workspace.remove(prA).active)
        assertNull(empty.add(prA).remove(prA).active)
    }

    @Test
    fun aFileHoldsOneMarkInEachSource() {
        val fingerprint = PatchFingerprint(PatchHash("m"), PatchHash("a1"))
        val reviewed = FileMark(prA, "Token.kt", fingerprint, MarkKind.Reviewed)
        val aside = reviewed.copy(kind = MarkKind.SetAside)
        val workspace = empty.mark(reviewed).mark(aside)
        assertEquals(listOf(aside), workspace.marks)
        assertEquals(aside, workspace.markOf(prA, "Token.kt"))
        assertNull(workspace.markOf(prB, "Token.kt"))
        assertNull(workspace.unmark(prA, "Token.kt").markOf(prA, "Token.kt"))
        assertEquals(2, workspace.mark(reviewed.copy(source = prB)).marks.size)
    }

    @Test
    fun theActiveSourceMustBeInTheWorkspace() {
        assertFailsWith<IllegalArgumentException> { Workspace(listOf(prA), active = prB) }
    }

    @Test
    fun aHeadIsRecordedOncePerSource() {
        val one = ObjectId("1".repeat(40))
        val base = ObjectId("b".repeat(40))
        val head = RecordedHead(one, base, 10)
        val workspace = empty.add(prA).record(prA, head).record(prA, head.copy(at = 20))
        assertEquals(listOf(head), workspace.headsOf(prA))
        assertEquals(emptyList(), workspace.headsOf(prB))
    }
}
