package dev.chieppa.arc.demo

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.host.conversation.ReviewConversation
import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SampleConversationTest {
    private val source = SourceRefs.parse("stretch@1234abcd..5678abcd")!!

    @Test
    fun theSampleIsWhatTheServiceLoaderFinds() {
        assertTrue(ServiceLoader.load(ReviewConversation::class.java).first() is SampleConversation)
    }

    @Test
    fun everyOtherPersonsThreadStateSitsOnItsOwnLine() {
        val threads = SampleConversation { 0 }.threads(source, "a/B.kt", ThreadContext((20..40).toList(), 60))
        assertEquals(setOf(ThreadState.Open, ThreadState.Addressed, ThreadState.Resolved, ThreadState.Guest), threads.map { it.state }.toSet())
        assertEquals(threads.size, threads.map { it.line }.toSet().size)
    }

    @Test
    fun revisionThreadsLandOnTheSourcesHeads() {
        val heads = ('1'..'5').map { ObjectId(it.toString().repeat(ObjectId.SHA1_LENGTH)) }
        val paths = listOf("b.kt", "a/One.kt", "a/Two.kt")
        val threads = SampleConversation { 0 }.revisionThreads(source, paths, heads)
        assertTrue(threads.all { it.madeOn in heads && it.path in paths })
        assertEquals(listOf(heads[0], heads[2], heads[4], heads[4]), threads.filter { it.path == "a/One.kt" }.map { it.madeOn })
        assertEquals(listOf(ThreadState.Addressed), threads.filter { it.path == "b.kt" }.map { it.state })
        assertEquals(emptyList(), SampleConversation { 0 }.revisionThreads(source, paths, emptyList()))
    }

    @Test
    fun aFileWithNoChangeHasNoThreads() {
        assertEquals(emptyList(), SampleConversation { 0 }.threads(source, "a/B.kt", ThreadContext(emptyList(), 60)))
    }

}
