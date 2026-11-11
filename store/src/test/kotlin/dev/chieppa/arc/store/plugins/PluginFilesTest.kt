package dev.chieppa.arc.store.plugins

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginsConfig
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class PluginFilesTest {
    private val dir = createTempDirectory("arc-plugin-files")
    private val files = PluginFiles(dir.resolve("plugins.json"), dir.resolve("plugin-store"))

    @Test
    fun keepsTheSetupAndEachStore() {
        assertEquals(PluginsConfig(), files.loadConfig())
        val config = PluginsConfig(
            sources = listOf(PluginSourceSpec.Directory("/code/todos"), PluginSourceSpec.Git("https://github.com/lumen/arc-speckit", "v0.4.2")),
            enabled = setOf("todos"),
            settings = mapOf("todos" to JsonObject(mapOf("folder" to JsonPrimitive("spec/**")))),
        )
        files.saveConfig(config)
        assertEquals(config, files.loadConfig())
        files.saveStore("todos", JsonObject(mapOf("seen" to JsonPrimitive(3))))
        assertEquals(JsonPrimitive(3), files.loadStore("todos")["seen"])
        assertEquals(JsonObject(emptyMap()), files.loadStore("other"))
    }
}
