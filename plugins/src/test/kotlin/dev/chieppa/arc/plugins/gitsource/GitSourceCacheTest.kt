package dev.chieppa.arc.plugins.gitsource

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitSourceCacheTest {
    private val dir = createTempDirectory("arc-git-cache")
    private val remote = TestRemote(dir)
    private val cache = GitSourceCache(dir.resolve("cache"))

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    @Test
    fun aRefResolvesAsABranchATagOrACommit() = runBlocking {
        val first = remote.commit(mapOf("plugin.json" to remote.manifest("0.4.2"), "src/index.tsx" to "// one"), tag = "v0.4.2")
        val second = remote.commit(mapOf("src/index.tsx" to "// two"))
        cache.fetch(remote.url)
        assertEquals(second, cache.resolve(PluginSourceSpec.Git(remote.url, "main")))
        assertEquals(first, cache.resolve(PluginSourceSpec.Git(remote.url, "v0.4.2")))
        assertEquals(first, cache.resolve(PluginSourceSpec.Git(remote.url, first)))
        assertNull(cache.resolve(PluginSourceSpec.Git(remote.url, "nowhere")))
    }

    @Test
    fun aCloneIsCheckedOutAtItsPinAndReadsANewerManifestWithoutCheckingItOut() = runBlocking {
        val first = remote.commit(mapOf("plugin.json" to remote.manifest("0.4.2"), "src/index.tsx" to "// one"))
        cache.fetch(remote.url)
        cache.checkOut(remote.url, first)
        val spec = PluginSourceSpec.Git(remote.url, "main", first)
        val folder = cache.checkout(spec)!!
        assertEquals("// one", Files.readString(folder.resolve("src/index.tsx")))
        val second = remote.commit(mapOf("plugin.json" to remote.manifest("0.5.0", """{"network":["api.github.com"]}"""), "src/index.tsx" to "// two"))
        cache.fetch(remote.url)
        assertEquals(second, cache.resolve(spec))
        assertEquals("0.5.0", cache.manifests(remote.url, second).getValue("speckit").version)
        assertEquals("// one", Files.readString(folder.resolve("src/index.tsx")))
        assertNull(cache.checkout(spec.copy(pinned = null)))
    }
}
