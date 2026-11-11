package dev.chieppa.arc.host.github.auth

/** A token from the first of [names] set in the environment that [env] reads. */
class EnvToken(private val names: List<String> = NAMES, private val env: (String) -> String? = System::getenv) : TokenSource {
    override fun token(): String? = set()?.let { env(it)?.trim() }

    override fun origin(): TokenOrigin = set()?.let { TokenOrigin.Environment(it) } ?: TokenOrigin.Other

    /** The first of [names] that holds a token. */
    private fun set(): String? = names.firstOrNull { name -> !env(name)?.trim().isNullOrEmpty() }

    companion object {
        val NAMES = listOf("GH_TOKEN", "GITHUB_TOKEN")
    }
}
