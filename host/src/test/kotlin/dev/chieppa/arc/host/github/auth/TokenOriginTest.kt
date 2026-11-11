package dev.chieppa.arc.host.github.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenOriginTest {
    @Test
    fun anEnvironmentTokenNamesTheFirstVariableThatIsSet() {
        val env = mapOf("GH_TOKEN" to "  ", "GITHUB_TOKEN" to "t2")
        val token = EnvToken(env = env::get)
        assertEquals("t2", token.token())
        assertEquals(TokenOrigin.Environment("GITHUB_TOKEN"), token.origin())
    }

    @Test
    fun theTokensSayWhichSourceGaveTheTokenInUse() {
        val gh = object : TokenSource {
            override fun token(): String? = null
            override fun origin(): TokenOrigin = TokenOrigin.GhCli
        }
        val tokens = GitHubTokens(listOf(gh, EnvToken(env = mapOf("GH_TOKEN" to "t1")::get)))
        assertEquals(TokenOrigin.Environment("GH_TOKEN"), tokens.tokenOrigin())
        assertEquals("t1", tokens.token())
    }

    @Test
    fun noTokenHasNoOrigin() {
        assertNull(GitHubTokens(listOf(TokenSource { null })).tokenOrigin())
    }
}
