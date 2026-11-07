package dev.chieppa.arc.host.github.auth

/**
 * The token GitHub requests use: the first of [sources] that has one, asked again only after [forget], such as when
 * GitHub refuses it. By default the `gh` login, then `GH_TOKEN` and `GITHUB_TOKEN`.
 */
class GitHubTokens(private val sources: List<TokenSource> = listOf(GhCliToken(), EnvToken())) : TokenSource {
    @Volatile
    private var cached: String? = null

    @Synchronized
    override fun token(): String? = cached ?: sources.firstNotNullOfOrNull { it.token() }.also { cached = it }

    /** Drops the token held, so the next request asks the sources again. */
    fun forget() {
        cached = null
    }
}
