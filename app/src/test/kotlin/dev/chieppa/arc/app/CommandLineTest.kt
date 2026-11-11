package dev.chieppa.arc.app

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.store.plugins.PluginFiles
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommandLineTest {
    private val dir = createTempDirectory("arc-cli")
    private val data = dir.resolve("data")
    private val out = ByteArrayOutputStream()
    private val err = ByteArrayOutputStream()
    private val files = PluginFiles(data.resolve(AppConstants.PLUGINS_FILE), data.resolve(AppConstants.PLUGIN_STORE_FOLDER))

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun run(vararg args: String): Int = runBlocking { CommandLine(data, dir, PrintStream(out), PrintStream(err)).run(args.toList()) }

    @Test
    fun pluginNewMakesAStarterAndAddsItsFolderAsASourceWithoutEnablingIt() {
        files.saveConfig(PluginsConfig(enabled = setOf("todos")))
        assertEquals(0, run("plugin", "new", "Todo", "Counter", "--in", "work"))
        val folder = dir.resolve("work/todo-counter").toAbsolutePath().normalize()
        assertTrue(Files.isRegularFile(folder.resolve("plugin.json")))
        assertEquals(PluginsConfig(sources = listOf(PluginSourceSpec.Directory(folder.toString())), enabled = setOf("todos")), files.loadConfig())
        assertTrue("todo-counter" in out.toString(), out.toString())
    }

    @Test
    fun aTakenFolderOrNoNameLeavesTheSetupAlone() {
        Files.createDirectories(dir.resolve("taken"))
        Files.writeString(dir.resolve("taken/a.txt"), "")
        assertEquals(1, run("plugin", "new", "taken"))
        assertEquals(2, run("plugin", "new"))
        assertEquals(PluginsConfig(), files.loadConfig())
        assertFalse(err.toString().isBlank())
    }

    @Test
    fun onlyThePluginCommandLeavesTheWindowUnopened() {
        assertTrue(CommandLine.handles(arrayOf("plugin", "new", "x")))
        assertFalse(CommandLine.handles(arrayOf()))
        assertFalse(CommandLine.handles(arrayOf("-psn_0_1")))
    }
}
