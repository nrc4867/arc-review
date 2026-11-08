package dev.chieppa.arc.host.github.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaRulesTest {
    @Test
    fun `the token goes to GitHub's own hosts only`() {
        assertTrue(MediaRules.sendsToken("https://github.com/user-attachments/assets/1234"))
        assertFalse(MediaRules.sendsToken("https://objects.githubusercontent.com/github-production-user-asset/1"))
        assertFalse(MediaRules.sendsToken("https://github.com.evil.example/x.png"))
        assertFalse(MediaRules.sendsToken("not a url"))
    }

    @Test
    fun `only https is fetched and only pictures are kept`() {
        assertTrue(MediaRules.fetchable("https://example.com/a.png"))
        assertFalse(MediaRules.fetchable("http://example.com/a.png"))
        assertFalse(MediaRules.fetchable("file:///etc/passwd"))
        assertTrue(MediaRules.keeps("image/png", 10))
        assertFalse(MediaRules.keeps("video/mp4", 10))
        assertFalse(MediaRules.keeps("image/png", MediaRules.MAX_BYTES + 1))
    }

    @Test
    fun `a relative redirect resolves against where it came from`() {
        assertEquals("https://github.com/b/c.png", MediaRules.resolve("https://github.com/a/x", "/b/c.png"))
    }
}
