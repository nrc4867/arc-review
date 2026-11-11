package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.console.FakeConsole
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.FakePlugin
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.host.WorkerSpec
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.PluginInfo
import dev.chieppa.arc.plugins.source.DirectoryWatcher
import dev.chieppa.arc.plugins.typecheck.TypeChecker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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

/** A directory plugin is type checked when enabled and after saves, beside it, and runs whatever its types say. */
@OptIn(ExperimentalCoroutinesApi::class)
class TypeChecksTest {
    private val dir = createTempDirectory("arc-type-checks")
    private val folder = dir.resolve("todos").also {
        Files.createDirectories(it)
        Files.writeString(it.resolve("plugin.json"), """{"id":"todos","version":"0.1.0","apiVersion":1,"entry":"src/index.tsx"}""")
    }
    private val source = PluginSourceSpec.Directory(folder.toAbsolutePath().normalize().toString())
    private val storage = PluginStorage.InMemory(PluginsConfig(listOf(source), setOf("todos"), mapOf("todos" to Capabilities())))
    private val changes = MutableSharedFlow<Unit>()
    private val error = PluginDiagnostic(DiagnosticKind.Type, "Property 'line' does not exist on type 'GrepHit'", "src/index.tsx", 14, 20)

    /** A checker played by the test: each check waits for the answer the test gives it, kept with where it ran. */
    private class Checks : TypeChecker {
        val asked = mutableListOf<Pair<Path, String>>()
        val answers = mutableListOf<CompletableDeferred<List<PluginDiagnostic>>>()
        var cancelled = 0

        override suspend fun check(folder: Path, entry: String): List<PluginDiagnostic> {
            asked += folder to entry
            val answer = CompletableDeferred<List<PluginDiagnostic>>().also { answers += it }
            try {
                return answer.await()
            } catch (e: CancellationException) {
                cancelled++
                throw e
            }
        }
    }

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun TestScope.manager(plugin: FakePlugin, checks: TypeChecker): PluginManager {
        val preparer = PluginPreparer { f, root, manifest, settings ->
            Preparation.Ready(WorkerSpec(manifest.id, root), InitializeParams(1, PluginInfo(manifest.id, manifest.version, f.toString()), manifest.entry, settings))
        }
        val services = PluginServices(
            backgroundScope, plugin, storage, ConsoleProcesses(FakeConsole(), backgroundScope), GitGrep({ _, _ -> CommandResult(1, "", "") }), preparer,
            watcher = DirectoryWatcher { changes }, clock = { testScheduler.currentTime }, typeChecker = checks, typeCheckMillis = WAIT,
        )
        return PluginManager(services).apply {
            start()
            runCurrent()
        }
    }

    @Test
    fun anEnabledPluginIsCheckedAfterAWaitAndRunsWithItsErrors() = runTest {
        val checks = Checks()
        val manager = manager(FakePlugin(backgroundScope), checks)
        assertEquals(emptyList(), checks.asked)
        advanceTimeBy(WAIT + 1)
        assertEquals(listOf(folder.toAbsolutePath().normalize() to "src/index.tsx"), checks.asked.map { it.first.toAbsolutePath().normalize() to it.second })
        checks.answers.single().complete(List(7) { error.copy(line = it + 1) })
        runCurrent()
        val found = manager.typeErrors.value.getValue("todos")
        assertEquals(7, found.total)
        assertEquals(5, found.shown.size)
        assertIs<PluginStatus.Running>(manager.instance("todos")!!.status.value)
    }

    @Test
    fun aSaveChecksAgainCancellingTheStaleCheck() = runTest {
        val checks = Checks()
        manager(FakePlugin(backgroundScope), checks)
        advanceTimeBy(WAIT + 1)
        changes.emit(Unit)
        advanceTimeBy(RELOAD + WAIT + 1)
        assertEquals(2, checks.asked.size)
        assertEquals(1, checks.cancelled)
    }

    @Test
    fun turningItOffForgetsItsErrors() = runTest {
        val checks = Checks()
        val manager = manager(FakePlugin(backgroundScope), checks)
        advanceTimeBy(WAIT + 1)
        checks.answers.single().complete(listOf(error))
        runCurrent()
        assertTrue("todos" in manager.typeErrors.value)
        manager.setEnabled("todos", false)
        runCurrent()
        assertNull(manager.typeErrors.value["todos"])
    }

    private companion object {
        const val WAIT = 1_000L
        const val RELOAD = 300L
    }
}
