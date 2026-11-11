package dev.chieppa.arc.plugins.console

import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleLinesTest {
    @Test
    fun cutsAfterEachLineBreakKeepingAPartialTail() {
        assertEquals(listOf("a\n", "\n", "b"), ConsoleLines.split(ConsoleChunk("a\n\nb")).map { it.text })
        assertEquals(listOf("a\n"), ConsoleLines.split(ConsoleChunk("a\n")).map { it.text })
        assertEquals(emptyList(), ConsoleLines.split(ConsoleChunk("")))
    }

    @Test
    fun keepsThePiecesOfTheLastLines() {
        val chunks = listOf("a\n", "b", "b\n", "c").map(::ConsoleChunk)
        assertEquals(listOf("b", "b\n", "c"), ConsoleLines.last(chunks, 2).map { it.text })
        assertEquals(listOf("c"), ConsoleLines.last(chunks, 1).map { it.text })
        assertEquals(chunks, ConsoleLines.last(chunks, 3))
        assertEquals(emptyList(), ConsoleLines.last(emptyList(), 3))
    }
}
