package dev.chieppa.arc.host.github.releases

/** One GitHub release: its tag's [version], whether it is a [prerelease] or a [draft], and its files. */
data class Release(val tag: String, val version: String, val prerelease: Boolean, val draft: Boolean, val assets: List<ReleaseAsset>) {
    /** The file called [name], or null. */
    fun asset(name: String): ReleaseAsset? = assets.firstOrNull { it.name == name }
}

/** One file of a release: its [name], [size], and the API [url] that downloads it. */
data class ReleaseAsset(val name: String, val size: Long, val url: String)
