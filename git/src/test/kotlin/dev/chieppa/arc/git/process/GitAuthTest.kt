package dev.chieppa.arc.git.process

import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GitAuthTest {
    private val home = Files.createTempDirectory("arc-auth")

    /** Runs the installed git's `credential fill` for github.com with [config] before it and [env], and gives what it answers. */
    private fun fill(config: List<String>, env: Map<String, String>): String {
        val process = ProcessBuilder(listOf("git") + config + listOf("credential", "fill")).directory(home.toFile()).apply {
            environment()["GIT_CONFIG_NOSYSTEM"] = "1"
            environment()["GIT_CONFIG_GLOBAL"] = home.resolve("gitconfig").toString()
            environment().putAll(GitSettings.ENVIRONMENT)
            environment().putAll(env)
        }.start()
        process.outputStream.use { it.write("protocol=https\nhost=github.com\npath=company/private.git\n\n".toByteArray()) }
        val out = process.inputStream.readAllBytes().toString(Charsets.UTF_8)
        check(process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) { "git credential fill did not end" }
        return out
    }

    @Test
    fun `the helper answers github with the token from the environment, in place of the person's helpers`() {
        val token = "gho_" + "k7".repeat(18)
        val persons = listOf("-c", "credential.helper=!f() { echo username=person; echo password=theirs; }; f")
        val answer = fill(persons + GitAuth.CONFIG, GitAuth.environment(token))
        assertTrue("username=${GitAuth.USER}" in answer, answer)
        assertTrue("password=$token" in answer, answer)
        assertFalse("theirs" in answer, answer)
    }

    @Test
    fun `the token is never in the config`() {
        val token = "gho_" + "k7".repeat(18)
        assertFalse(GitAuth.CONFIG.any { token in it })
    }

    @Test
    fun `only commands that reach a remote sign in`() {
        assertTrue(GitAuth.reachesRemote(listOf("clone", "url", "dir")))
        assertTrue(GitAuth.reachesRemote(listOf("-c", "fetch.negotiationAlgorithm=noop", "fetch", "origin")))
        assertFalse(GitAuth.reachesRemote(listOf("cat-file", "--batch")))
        assertFalse(GitAuth.reachesRemote(listOf("-c", "a=b", "log", "fetch")))
        assertEquals(false, GitAuth.reachesRemote(emptyList()))
    }

    private companion object {
        const val TIMEOUT_SECONDS = 30L
    }
}
