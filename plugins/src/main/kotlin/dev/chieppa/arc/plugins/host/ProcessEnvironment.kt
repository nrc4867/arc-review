package dev.chieppa.arc.plugins.host

/** The environment a started process gets. */
object ProcessEnvironment {
    /** Arc's own secrets, which no process it starts for a plugin is given. */
    val SECRETS: Set<String> = setOf("GH_TOKEN", "GITHUB_TOKEN", "GH_ENTERPRISE_TOKEN", "GITHUB_ENTERPRISE_TOKEN")

    /** Sets [target], a process's environment as it starts from Arc's, to what [spec] gives it. */
    fun apply(spec: LaunchSpec, target: MutableMap<String, String>) {
        val exact = spec.environment
        if (exact != null) {
            target.clear()
            target.putAll(exact)
        } else {
            target.keys.removeAll { name -> SECRETS.any { it.equals(name, ignoreCase = true) } }
        }
    }
}
