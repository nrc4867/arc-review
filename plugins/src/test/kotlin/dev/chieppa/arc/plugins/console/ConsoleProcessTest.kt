package dev.chieppa.arc.plugins.console

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ConsoleProcessTest {
    private fun text(process: ConsoleProcess): String = process.output.value.chunks.joinToString("") { it.text }

    @Test
    fun aPromptWithNoLineBreakShowsAtOnceAndLaterOutputExtendsIt() = runTest {
        val console = FakeConsole()
        val process = ConsoleProcess("p-1", "todos", listOf("login"), console, backgroundScope)
        console.print("Password: ")
        runCurrent()
        assertEquals("Password: ", text(process))
        console.print("ok\nContinue? [y/N] ")
        runCurrent()
        assertEquals("Password: ok\nContinue? [y/N] ", text(process))
        assertEquals(3L, process.output.value.total)
    }

    @Test
    fun keepsTheLastLinesCountingAPartialOne() = runTest {
        val console = FakeConsole()
        val process = ConsoleProcess("p-1", "todos", listOf("build"), console, backgroundScope, maxLines = 2)
        console.print("one\ntwo\nthr")
        console.print("ee\nfour")
        runCurrent()
        assertEquals("three\nfour", text(process))
        assertEquals(5L, process.output.value.total)
    }

    @Test
    fun passesEachPieceOnAsItComesAndTheEnd() = runTest {
        val console = FakeConsole()
        val pieces = mutableListOf<ConsoleChunk>()
        var exit: Int? = null
        ConsoleProcess("p-1", "todos", listOf("npm"), console, backgroundScope, onChunk = { pieces += it }, onExit = { exit = it })
        console.print("Continue? ", error = true)
        console.end(1)
        runCurrent()
        assertEquals(listOf(ConsoleChunk("Continue? ", error = true)), pieces)
        assertEquals(1, exit)
    }
}
