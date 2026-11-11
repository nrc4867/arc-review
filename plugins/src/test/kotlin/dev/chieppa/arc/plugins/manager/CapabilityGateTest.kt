package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.CommentsAccess
import dev.chieppa.arc.plugins.manifest.NotesAccess
import dev.chieppa.arc.plugins.protocol.AppEvents
import dev.chieppa.arc.plugins.manifest.FileAccess
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.RpcException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CapabilityGateTest {
    private val github = Capabilities(network = listOf("api.github.com", "*.example.com"))

    @Test
    fun aCallNeedsWhatItsMethodNeeds() {
        CapabilityGate.check(Methods.NET_FETCH, github)
        CapabilityGate.check(Methods.STORE_GET, Capabilities())
        CapabilityGate.check(Methods.FILES_READ, Capabilities(files = FileAccess(read = listOf("docs"))))
        for (method in listOf(Methods.NET_FETCH, Methods.FILES_READ, Methods.FILES_WRITE, Methods.CONSOLE_RUN, Methods.GIT_GREP)) {
            assertEquals(ErrorCodes.DENIED, assertFailsWith<RpcException> { CapabilityGate.check(method, Capabilities()) }.error.code)
        }
    }

    @Test
    fun notesAndDraftsNeedTheirOwnCapabilities() {
        CapabilityGate.check(Methods.NOTES_LIST, Capabilities(notes = NotesAccess(read = true)))
        CapabilityGate.check(Methods.NOTES_CREATE, Capabilities(notes = NotesAccess(write = true)))
        CapabilityGate.check(Methods.COMMENTS_DRAFT, Capabilities(comments = CommentsAccess(draft = true)))
        assertFailsWith<RpcException> { CapabilityGate.check(Methods.NOTES_LIST, Capabilities(notes = NotesAccess(write = true))) }
        assertFailsWith<RpcException> { CapabilityGate.check(Methods.NOTES_CREATE, Capabilities(notes = NotesAccess(read = true))) }
        assertFailsWith<RpcException> { CapabilityGate.check(Methods.COMMENTS_DRAFT, Capabilities(notes = NotesAccess(read = true, write = true))) }
    }

    @Test
    fun onlyAPluginThatReadsNotesHearsThemChange() {
        assertTrue(CapabilityGate.hears(AppEvents.NOTES_CHANGED, Capabilities(notes = NotesAccess(read = true))))
        assertFalse(CapabilityGate.hears(AppEvents.NOTES_CHANGED, Capabilities(notes = NotesAccess(write = true))))
        assertTrue(CapabilityGate.hears(AppEvents.SOURCE_OPENED, Capabilities()))
    }

    @Test
    fun fetchReachesOnlyTheHostsTheManifestNames() {
        assertTrue(CapabilityGate.allowsHost("https://api.github.com/repos/a/b", github))
        assertTrue(CapabilityGate.allowsHost("https://API.GitHub.com/", github))
        assertTrue(CapabilityGate.allowsHost("https://docs.example.com/x", github))
        assertFalse(CapabilityGate.allowsHost("https://github.com/", github))
        assertFalse(CapabilityGate.allowsHost("https://api.github.com.evil.io/", github))
        assertFalse(CapabilityGate.allowsHost("https://api.github.com@evil.io/", github))
        assertFalse(CapabilityGate.allowsHost("file:///etc/passwd", github))
        assertFalse(CapabilityGate.allowsHost("not a url", github))
        assertFailsWith<RpcException> { CapabilityGate.checkHost("https://evil.io/", github) }
    }
}
