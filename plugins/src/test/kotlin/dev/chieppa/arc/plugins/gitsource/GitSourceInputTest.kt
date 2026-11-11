package dev.chieppa.arc.plugins.gitsource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitSourceInputTest {
    @Test
    fun aUrlWithARefAfterAHashIsAGitSource() {
        assertEquals("https://github.com/lumen/arc-speckit" to "v0.4.2", GitSourceInput.parse(" https://github.com/lumen/arc-speckit#v0.4.2 "))
        assertEquals("git@github.com:lumen/arc-team-plugins.git" to "HEAD", GitSourceInput.parse("git@github.com:lumen/arc-team-plugins.git"))
        assertEquals("file:///C:/repos/plugins" to "main", GitSourceInput.parse("file:///C:/repos/plugins#main"))
    }

    @Test
    fun aFolderIsNot() {
        assertNull(GitSourceInput.parse("C:\\code\\arc-plugins\\todos"))
        assertNull(GitSourceInput.parse("~/code/arc-plugins"))
        assertNull(GitSourceInput.parse(""))
    }
}
