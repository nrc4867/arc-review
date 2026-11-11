package dev.chieppa.arc.plugins.gitsource

import dev.chieppa.arc.git.process.GitException
import dev.chieppa.arc.git.process.GitRunner
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.manifest.ManifestFiles
import dev.chieppa.arc.plugins.manifest.ManifestReader
import dev.chieppa.arc.plugins.manifest.ManifestResult
import dev.chieppa.arc.plugins.manifest.PluginManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * Arc's own clones of git plugin sources, one folder per url under [root], made and read with the installed git
 * through [git]. A source's plugins run from its clone checked out at the commit it is pinned to; newer commits are
 * fetched and read without being checked out until the person accepts them. Every call throws [GitException] when git
 * fails.
 */
class GitSourceCache(private val root: Path, private val git: GitRunner = GitRunner()) {
    /** The folder [url] is cloned in: its repo's name and a hash of the whole url. */
    fun folder(url: String): Path {
        val digest = MessageDigest.getInstance(SHA_256).digest(url.toByteArray(Charsets.UTF_8))
        val name = url.trimEnd('/').substringAfterLast('/').substringAfterLast(':').removeSuffix(GIT_SUFFIX).replace(NOT_IN_NAME, DASH).trim('-', '.')
        return root.resolve(name.ifEmpty { FALLBACK } + DASH + digest.take(NAME_BYTES).joinToString("") { "%02x".format(it) })
    }

    /** The folder [spec]'s plugins run from: its clone, once it is pinned and cloned. */
    fun checkout(spec: PluginSourceSpec.Git): Path? = folder(spec.url).takeIf { spec.pinned != null && Files.isDirectory(it.resolve(GIT_DIR)) }

    /** Clones [url] when it is not cloned yet, else fetches its branches and tags; true when it cloned it. */
    suspend fun fetch(url: String): Boolean {
        val folder = folder(url)
        if (Files.isDirectory(folder.resolve(GIT_DIR))) {
            git.run(folder, listOf("fetch", "--quiet", "--force", "--tags", "--prune", ORIGIN))
            return false
        }
        withContext(Dispatchers.IO) { Files.createDirectories(root) }
        if (Files.exists(folder)) folder.toFile().deleteRecursively()
        try {
            git.run(root, listOf("clone", "--quiet", "--no-checkout", url, folder.toAbsolutePath().toString()))
        } catch (e: GitException) {
            folder.toFile().deleteRecursively()
            throw e
        }
        return true
    }

    /** The commit [spec]'s ref names now, as last fetched: a branch, a tag or a commit; null when it names none. */
    suspend fun resolve(spec: PluginSourceSpec.Git): String? {
        val folder = folder(spec.url)
        for (name in listOf("$REMOTES${spec.ref}", "$TAGS${spec.ref}", spec.ref)) {
            val result = git.run(folder, listOf("rev-parse", "--verify", "--quiet", "$name$COMMIT"), accept = MISSING_OK)
            if (result.exitCode == 0) return result.text.trim()
        }
        return null
    }

    /** Checks [url]'s clone out at [sha], leaving nothing else in it. */
    suspend fun checkOut(url: String, sha: String) {
        val folder = folder(url)
        git.run(folder, listOf("checkout", "--quiet", "--force", "--detach", sha))
        git.run(folder, listOf("clean", "--quiet", "-ffdx"))
    }

    /** The manifests of the plugins [url]'s clone holds at [sha], read from git without checking it out. */
    suspend fun manifests(url: String, sha: String): Map<String, PluginManifest> {
        val folder = folder(url)
        val list = show(folder, sha, ManifestFiles.PLUGINS)
        val folders = list?.let { (ManifestReader.list(it) as? ManifestResult.Read)?.value?.plugins } ?: listOf("")
        return folders.mapNotNull { path ->
            val name = listOf(path.trim('/'), ManifestFiles.PLUGIN).filter { it.isNotEmpty() && it != CURRENT }.joinToString("/")
            val text = show(folder, sha, name) ?: return@mapNotNull null
            (ManifestReader.plugin(text) as? ManifestResult.Read)?.value?.let { it.id to it }
        }.toMap()
    }

    /** Forgets [url]'s clone. */
    suspend fun remove(url: String) {
        withContext(Dispatchers.IO) { folder(url).toFile().deleteRecursively() }
    }

    private suspend fun show(folder: Path, sha: String, path: String): String? {
        val result = git.run(folder, listOf("show", "$sha:$path"), accept = SHOW_OK)
        return result.text.takeIf { result.exitCode == 0 }
    }

    private companion object {
        const val SHA_256 = "SHA-256"
        const val NAME_BYTES = 4
        const val GIT_SUFFIX = ".git"
        const val DASH = "-"
        const val FALLBACK = "source"
        val NOT_IN_NAME = Regex("[^A-Za-z0-9._-]+")
        const val GIT_DIR = ".git"
        const val ORIGIN = "origin"
        const val REMOTES = "refs/remotes/origin/"
        const val TAGS = "refs/tags/"
        const val COMMIT = "^{commit}"
        const val CURRENT = "."
        val MISSING_OK = setOf(0, 1, 128)
        val SHOW_OK = setOf(0, 128)
    }
}
