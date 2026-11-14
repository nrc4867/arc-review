package dev.chieppa.arc.core.activity

import kotlin.test.Test
import kotlin.test.assertEquals

class FailureHeadlineTest {
    @Test
    fun `git's fatal line is the headline`() {
        val stderr = "Cloning into 'x'...\nremote: Repository not found.\nfatal: repository 'https://github.com/co/x.git/' not found\n"
        assertEquals("fatal: repository 'https://github.com/co/x.git/' not found", failureHeadline(stderr))
    }

    @Test
    fun `other text gives its first line`() {
        assertEquals("Releases answered 404", failureHeadline("\n  Releases answered 404\nNot Found"))
        assertEquals("", failureHeadline(""))
    }
}
