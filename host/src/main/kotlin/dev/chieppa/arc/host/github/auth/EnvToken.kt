package dev.chieppa.arc.host.github.auth

/** A token from the first of [names] set in the environment that [env] reads. */
class EnvToken(private val names: List<String> = NAMES, private val env: (String) -> String? = System::getenv) : TokenSource {
    override fun token(): String? = names.firstNotNullOfOrNull { name -> env(name)?.trim()?.takeIf { it.isNotEmpty() } }

    companion object {
        val NAMES = listOf("GH_TOKEN", "GITHUB_TOKEN")
    }
}
