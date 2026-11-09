package dev.chieppa.arc.core.actions.log

import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunSamples
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogFilterTest {
    private fun stamp(second: Long) = RunSamples.T0.plusSeconds(second).toString().replace("Z", ".1234567Z")

    @Test
    fun `keeps each step's tail, more of a failed one`() {
        val steps = listOf(RunSamples.step(1, "Build", 0, 99), RunSamples.step(2, "Test", 100, 200, RunConclusion.Failure))
        val filter = LogFilter(steps, LogLimits(tail = 2, failedTail = 3, silences = 0))
        (0 until 10).forEach { filter.accept("${stamp(it.toLong())} build $it") }
        (0 until 10).forEach { filter.accept("${stamp(100L + it)} test $it") }
        val kept = filter.finish()
        assertEquals(listOf("build 8", "build 9"), kept.step(1).map { it.text })
        assertEquals(listOf("test 7", "test 8", "test 9"), kept.step(2).map { it.text })
        assertEquals(20, kept.totalLines)
    }

    @Test
    fun `keeps an error with the lines before it`() {
        val filter = LogFilter(listOf(RunSamples.step(1, "Build", 0, 99)), LogLimits(tail = 1, beforeProblem = 2, silences = 0))
        listOf("a", "b", "c", "##[error]boom", "d", "e").forEachIndexed { i, text -> filter.accept("${stamp(i.toLong())} $text") }
        assertEquals(listOf("b", "c", "##[error]boom", "e"), filter.finish().lines.map { it.text })
    }

    @Test
    fun `keeps the longest silences with the lines either side`() {
        val filter = LogFilter(listOf(RunSamples.step(1, "Wait", 0, 9999)), LogLimits(tail = 0, silences = 1, aroundSilence = 1))
        val times = listOf(0L, 1L, 2L, 600L, 601L, 602L, 640L, 641L)
        times.forEachIndexed { i, at -> filter.accept("${stamp(at)} line $i") }
        val kept = filter.finish()
        assertEquals(listOf(LogSilence(3, 598)), kept.silences)
        assertEquals(listOf("line 2", "line 3"), kept.lines.map { it.text })
    }

    @Test
    fun `lines without a time keep the last time and the byte order mark goes`() {
        val filter = LogFilter(listOf(RunSamples.step(1, "Build", 0, 99)))
        filter.accept("﻿${stamp(1)} first")
        filter.accept("continued")
        val lines = filter.finish().lines
        assertEquals("first", lines[0].text)
        assertTrue(lines[1].epochSecond == lines[0].epochSecond)
    }
}
