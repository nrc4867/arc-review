package dev.chieppa.arc.plugins.runtime

import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.host.HostFiles
import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.ProcessEnvironment
import dev.chieppa.arc.plugins.host.SystemProcessLauncher
import dev.chieppa.arc.tools.ToolInstaller
import dev.chieppa.arc.tools.ToolPins
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The plugins' Node process starts with the same few permissions for every plugin and none of Arc's environment. */
class NodeLaunchTest {
    private val dir = createTempDirectory("arc-node-launch")
    private val node = dir.resolve("node-v26").resolve("node")
    private val host = HostFiles(dir.resolve("runtime/host-1"), dir.resolve("runtime/host-1/host.js"))
    private val sources = dir.resolve("plugins")
    private val parent = mapOf(
        "GH_TOKEN" to "gho_secret",
        "GITHUB_TOKEN" to "ghp_secret",
        "NODE_OPTIONS" to "--require /tmp/evil.js",
        "HOME" to "/home/nicholas",
        "SystemRoot" to "C:\\Windows",
        "TEMP" to "C:\\Temp",
        "TMPDIR" to "/tmp",
        "PATH" to "/usr/bin",
    )

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun tools(version: String) = PluginTools(ToolInstaller(dir, { _, _, _, _ -> throw IOException("offline") }), ToolPins(Properties()), { _, _ -> CommandResult(0, version, "") }, node)

    private suspend fun spec(): LaunchSpec = NodeLaunch(tools("v26.13.0"), { host }, dir.resolve("work"), parent).spec(setOf(sources))

    @Test
    fun theEnvironmentHoldsOnlyWhatNodeNeeds() = runTest {
        val environment = spec().environment!!
        for (secret in ProcessEnvironment.SECRETS + "NODE_OPTIONS" + "HOME") assertTrue(secret !in environment, secret)
        assertEquals(node.toAbsolutePath().parent.toString(), environment["PATH"])
        assertEquals(mapOf("SystemRoot" to "C:\\Windows", "TEMP" to "C:\\Temp"), NodeEnvironment.of(parent, node.parent, windows = true) - "PATH")
        assertEquals(mapOf("TMPDIR" to "/tmp"), NodeEnvironment.of(parent, node.parent, windows = false) - "PATH")
    }

    @Test
    fun theProcessGetsExactlyThatEnvironmentInANeutralFolder() = runTest {
        val spec = spec()
        val started = SystemProcessLauncher.builder(spec)
        assertEquals(spec.environment, started.environment())
        assertEquals(dir.resolve("work").toFile(), started.directory())
    }

    @Test
    fun itReadsOnlyTheSourcesAndTheHostAndStartsWorkers() = runTest {
        val command = spec().command
        val flags = command.filter { it.startsWith("--allow") }
        assertEquals(
            listOf("--allow-fs-read=${host.folder.toAbsolutePath()}", "--allow-fs-read=${sources.toAbsolutePath()}", "--allow-worker").sorted(),
            flags.sorted(),
        )
        assertEquals(listOf(node.toString(), "--permission"), command.take(2))
        assertEquals(host.script.toString(), command.last())
    }

    @Test
    fun aNodeTooOldToKeepPluginsOffTheNetworkIsRefused() = runTest {
        assertFailsWith<IOException> { NodeLaunch(tools("v24.16.0"), { host }, dir.resolve("work"), parent).spec(setOf(sources)) }
    }

    @Test
    fun aProgramStartedForAPluginGetsArcsEnvironmentWithoutItsSecrets() {
        val started = SystemProcessLauncher.builder(LaunchSpec(listOf("git"), dir))
        assertTrue(ProcessEnvironment.SECRETS.none { name -> started.environment().keys.any { it.equals(name, ignoreCase = true) } })
    }
}
