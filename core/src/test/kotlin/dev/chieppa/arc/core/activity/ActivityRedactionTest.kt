package dev.chieppa.arc.core.activity

import kotlin.test.Test
import kotlin.test.assertEquals

class ActivityRedactionTest {
    @Test
    fun `masks github tokens by prefix`() {
        assertEquals("token *** and ***", ActivityRedaction.text("token gho_ABCDEFGH1234 and github_pat_11ABCDEFG_xyz"))
    }

    @Test
    fun `masks bearer values and secret query parameters`() {
        assertEquals("Bearer *** sent", ActivityRedaction.text("Bearer abcdefghijklmnop sent"))
        assertEquals("GET /a?access_token=***&page=2", ActivityRedaction.text("GET /a?access_token=s3cr3t&page=2"))
    }

    @Test
    fun `masks a url's password but keeps plain urls`() {
        assertEquals("https://***@example.com/r.git", ActivityRedaction.text("https://me:hunter2@example.com/r.git"))
        assertEquals("fatal: repository 'https://github.com/o/n.git/' not found", ActivityRedaction.text("fatal: repository 'https://github.com/o/n.git/' not found"))
    }
}
