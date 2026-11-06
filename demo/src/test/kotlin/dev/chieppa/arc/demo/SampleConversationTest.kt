package dev.chieppa.arc.demo

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
    fun everyThreadStateSitsOnItsOwnLine() {
        val threads = SampleConversation { 0 }.threads(source, "a/B.kt", ThreadContext((20..40).toList(), 60))
        assertEquals(ThreadState.entries.toSet(), threads.map { it.state }.toSet())
        assertEquals(threads.size, threads.map { it.line }.toSet().size)
    }

    @Test
    fun aFileWithNoChangeHasNoThreads() {
        assertEquals(emptyList(), SampleConversation { 0 }.threads(source, "a/B.kt", ThreadContext(emptyList(), 60)))
    }

    @Test
    fun stagedCommentsAreOnTheReviewsFiles() {
        val staged = SampleConversation().staged(mapOf(source to listOf("a/B.kt", "c/D.kt")))
        assertTrue(staged.isNotEmpty())
        assertTrue(staged.all { it.path in setOf("a/B.kt", "c/D.kt") })
        assertTrue(staged.any { it.lineGoneIn != null })
        assertEquals(emptyList(), SampleConversation().staged(emptyMap()))
    }
}
