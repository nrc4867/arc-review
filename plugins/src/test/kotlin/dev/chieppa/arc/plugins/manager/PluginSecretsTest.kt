package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.console.FakeConsole
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.FakePlugin
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.host.WorkerSpec
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.PluginInfo
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.SettingSpec
import dev.chieppa.arc.plugins.protocol.SettingType
import dev.chieppa.arc.plugins.protocol.SettingsChangedParams
import dev.chieppa.arc.plugins.secrets.MemorySecretStore
import dev.chieppa.arc.plugins.secrets.SecretStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A plugin's secret settings go to the keychain, never to plugins.json, and only that plugin is given them. */
@OptIn(ExperimentalCoroutinesApi::class)
class PluginSecretsTest {
    private val dir = createTempDirectory("arc-plugin-secrets")
    private val folder = dir.resolve("todos").also {
        Files.createDirectories(it)
        Files.writeString(it.resolve("plugin.json"), """{"id":"todos","version":"0.1.0","apiVersion":1,"entry":"index.tsx"}""")
    }
    private val source = PluginSourceSpec.Directory(folder.toAbsolutePath().normalize().toString())
    private val contributions = Contributions(settings = mapOf("token" to SettingSpec(SettingType.Secret), "folder" to SettingSpec(SettingType.Text)))
    private val storage = PluginStorage.InMemory(PluginsConfig(listOf(source), setOf("todos"), mapOf("todos" to Capabilities())))

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun TestScope.manager(plugin: FakePlugin, secrets: SecretStore): PluginManager {
        val preparer = PluginPreparer { f, root, manifest, settings ->
            Preparation.Ready(WorkerSpec(manifest.id, root), InitializeParams(1, PluginInfo(manifest.id, manifest.version, f.toString()), "index.tsx", settings))
        }
        val services = PluginServices(
            backgroundScope, plugin, storage, ConsoleProcesses(FakeConsole(), backgroundScope), GitGrep({ _, _ -> CommandResult(1, "", "") }), preparer,
            clock = { testScheduler.currentTime }, secrets = secrets,
        )
        return PluginManager(services).apply {
            start()
            runCurrent()
        }
    }

    private fun TestScope.set(plugin: FakePlugin, id: Long, key: String, value: String) {
        plugin.current.write(Envelope.Request(id, Methods.SETTINGS_SET, buildJsonObject { put("key", key); put("value", value) }))
        runCurrent()
    }

    @Test
    fun aSecretGoesToTheKeychainAndBackOnlyToItsPlugin() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val keychain = MemorySecretStore(lasting = true)
        val manager = manager(plugin, keychain)
        set(plugin, 1, "token", "ghp_abc")
        set(plugin, 2, "folder", "spec")
        assertEquals("ghp_abc", keychain.read("todos", "token"))
        val config = storage.loadConfig()
        assertEquals(setOf("token"), config.secrets["todos"])
        assertNull(config.settings.getValue("todos")["token"])
        assertEquals(JsonPrimitive("spec"), config.settings.getValue("todos")["folder"])
        val changed = plugin.received.filterIsInstance<Envelope.Notification>().last { it.method == Methods.SETTINGS_CHANGED }
        assertEquals(JsonPrimitive("ghp_abc"), ProtocolJson.fromElement(SettingsChangedParams.serializer(), changed.params).values["token"])
        assertTrue(manager.secretsLast)
    }

    @Test
    fun clearingASecretForgetsItAndRemovingThePluginForgetsTheRest() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val keychain = MemorySecretStore(lasting = true)
        val manager = manager(plugin, keychain)
        set(plugin, 1, "token", "ghp_abc")
        set(plugin, 2, "token", "")
        assertNull(keychain.read("todos", "token"))
        assertNull(storage.loadConfig().secrets["todos"])
        set(plugin, 3, "token", "ghp_def")
        manager.removeSource(source)
        runCurrent()
        assertNull(keychain.read("todos", "token"))
        assertNull(storage.loadConfig().secrets["todos"])
    }

    @Test
    fun aKeychainThatRefusesKeepsTheSecretForThisRunOnly() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val refusing = object : SecretStore {
            override val lasting = true

            override fun read(plugin: String, key: String): String? = throw IOException("locked")

            override fun write(plugin: String, key: String, value: String) = throw IOException("locked")

            override fun delete(plugin: String, key: String) = throw IOException("locked")
        }
        val manager = manager(plugin, refusing)
        set(plugin, 1, "token", "ghp_abc")
        assertEquals(JsonPrimitive("ghp_abc"), manager.settings("todos")["token"])
        assertFalse(manager.secretsLast)
        assertFalse(MemorySecretStore().lasting)
    }
}
