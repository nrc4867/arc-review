package dev.chieppa.arc.core.actions.artifacts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JUnitReportTest {
    @Test
    fun `reads cases with failures first and where they failed`() {
        val xml = """
            <testsuite name="TokenServiceTest" tests="2">
              <testcase classname="TokenServiceTest" name="reused token answers 409" time="0.12"/>
              <testcase classname="TokenServiceTest" name="concurrent refreshes" time="0.41">
                <failure message="expected: &lt;1&gt; but was: &lt;2&gt;">AssertionFailedError
                  at com.lumen.TokenServiceTest.concurrent(TokenServiceTest.kt:88)</failure>
              </testcase>
            </testsuite>
        """.trimIndent()
        val report = JUnitReport.parse(xml.toByteArray())!!
        assertEquals(1, report.failures)
        assertEquals("concurrent refreshes", report.cases[0].name)
        assertEquals("TokenServiceTest.kt:88", report.cases[0].location)
        assertEquals("expected: <1> but was: <2>", report.cases[0].failure)
    }

    @Test
    fun `a doctype is refused`() {
        assertNull(JUnitReport.parse("<!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><testsuite>&e;</testsuite>".toByteArray()))
    }

    @Test
    fun `kinds come from the name and a JUnit report from its first bytes`() {
        assertEquals(ArtifactKind.JUnit, ArtifactKind.of("TEST-x.xml", "<?xml version=\"1.0\"?><testsuite>".toByteArray()))
        assertEquals(ArtifactKind.Markdown, ArtifactKind.of("summary.md"))
        assertEquals(ArtifactKind.Html, ArtifactKind.of("index.html"))
    }
}
