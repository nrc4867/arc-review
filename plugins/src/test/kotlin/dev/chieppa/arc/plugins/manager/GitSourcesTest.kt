package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.console.FakeConsole
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.gitsource.GitSourceCache
import dev.chieppa.arc.plugins.gitsource.TestRemote
import dev.chieppa.arc.plugins.host.FakePlugin
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.host.WorkerSpec
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.PluginInfo
import dev.chieppa.arc.plugins.source.DirectoryWatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A git source is cloned, pinned, checked for new commits on start and daily, and moves only when the person accepts. */
@OptIn(ExperimentalCoroutinesApi::class)
class GitSourcesTest {
    private val dir = createTempDirectory("arc-git-sources")
    private val remote = TestRemote(dir)
    private val storage = PluginStorage.InMemory(PluginsConfig(enabled = setOf("speckit"), approved = mapOf("speckit" to Capabilities(panels = true))))
    private val watched = mutableListOf<Path>()

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun TestScope.manager(plugin: FakePlugin): PluginManager {
        val preparer = PluginPreparer { f, root, manifest, settings ->
            Preparation.Ready(WorkerSpec(manifest.id, root), InitializeParams(1, PluginInfo(manifest.id, manifest.version, f.toString()), manifest.entry, settings))
        }
        val services = PluginServices(
            backgroundScope, plugin, storage, ConsoleProcesses(FakeConsole(), backgroundScope), GitGrep({ _, _ -> CommandResult(1, "", "") }), preparer,
            watcher = DirectoryWatcher { watched.add(it); emptyFlow() }, clock = { testScheduler.currentTime }, gitSources = GitSourceCache(dir.resolve("cache")),
            gitCheckMillis = DAY,
        )
        return PluginManager(services).apply { start() }
    }

    private suspend fun PluginManager.pinned() = config.first { pinnedOf(this) != null }

    private fun pinnedOf(manager: PluginManager): String? = manager.config.value.sources.filterIsInstance<PluginSourceSpec.Git>().single().pinned

    @Test
    fun aSourceIsClonedPinnedAndRunsReadOnlyFromItsClone() = runTest {
        val tagged = remote.commit(mapOf("plugin.json" to remote.manifest("0.4.2", """{"panels":true}"""), "src/index.tsx" to "// one"), tag = "v0.4.2")
        remote.commit(mapOf("src/index.tsx" to "// two"))
        val plugin = FakePlugin(backgroundScope)
        val manager = manager(plugin)
        manager.addGit(remote.url, "v0.4.2")
        manager.pinned()
        manager.entries.first { it.isNotEmpty() }
        runCurrent()
        assertEquals(tagged, pinnedOf(manager))
        assertIs<PluginStatus.Running>(manager.instance("speckit")!!.status.value)
        assertTrue(plugin.workers.single().readRoot.startsWith(dir.resolve("cache")))
        assertEquals(emptyList(), watched)
    }

    @Test
    fun aNewCommitWaitsAsAnUpdateAndOneAskingForMoreStaysOffOnceAccepted() = runTest {
        val first = remote.commit(mapOf("plugin.json" to remote.manifest("0.4.2", """{"panels":true}"""), "src/index.tsx" to "// one"))
        val manager = manager(FakePlugin(backgroundScope))
        manager.addGit(remote.url, "main")
        manager.pinned()
        val second = remote.commit(mapOf("plugin.json" to remote.manifest("0.5.0", """{"panels":true,"network":["api.github.com"]}""")))
        advanceTimeBy(DAY + 1)
        val update = manager.gitStates.first { it[remote.url]?.update != null }.getValue(remote.url).update!!
        assertEquals(second, update.sha)
        assertEquals("0.5.0", update.version)
        assertEquals(listOf<Capability>(Capability.Network("api.github.com")), update.asks)
        assertEquals(first, pinnedOf(manager))
        assertEquals("0.4.2", manager.entries.value.single().found.manifest?.version)
        manager.acceptUpdate(remote.url)
        val entry = manager.entries.first { it.singleOrNull()?.found?.manifest?.version == "0.5.0" }.single()
        assertEquals(second, pinnedOf(manager))
        assertEquals("0.5.0", entry.found.manifest?.version)
        assertNull(entry.instance)
        assertEquals(listOf<Capability>(Capability.Network("api.github.com")), entry.asks)
        assertNull(manager.gitStates.value.getValue(remote.url).update)
    }

    @Test
    fun removingASourceForgetsItsClone() = runTest {
        remote.commit(mapOf("plugin.json" to remote.manifest("0.4.2"), "src/index.tsx" to "// one"))
        val manager = manager(FakePlugin(backgroundScope))
        manager.addGit(remote.url, "main")
        manager.pinned()
        val clone = manager.gitClone(remote.url)!!
        assertTrue(Files.isDirectory(clone))
        manager.removeSource(manager.config.value.sources.single())
        runCurrent()
        assertTrue(manager.entries.value.isEmpty())
    }

    private companion object {
        const val DAY = 24L * 60 * 60 * 1000
    }
}
