package dev.chieppa.arc.plugins.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ManifestReaderTest {
    @Test
    fun readsAManifestWithItsCapabilities() {
        val text = """
            {"id": "todos", "version": "0.1.0", "apiVersion": 1, "entry": "src/index.tsx", "name": "TODOs",
             "capabilities": {"panels": true, "console": true, "git": true, "files": {"read": ["docs"]}, "network": ["api.github.com"]},
             "future": "ignored"}
        """.trimIndent()
        val manifest = assertIs<ManifestResult.Read<PluginManifest>>(ManifestReader.plugin(text)).value
        assertEquals("TODOs", manifest.title)
        assertEquals(
            listOf(Capability.Panels, Capability.Console, Capability.Git, Capability.ReadFiles("docs"), Capability.Network("api.github.com")),
            Capability.of(manifest.capabilities),
        )
    }

    @Test
    fun refusesABadIdAnUnknownApiVersionAndBrokenJson() {
        assertIs<ManifestResult.Invalid>(ManifestReader.plugin("""{"id": "Bad Id", "version": "1", "apiVersion": 1, "entry": "a.ts"}"""))
        assertIs<ManifestResult.Invalid>(ManifestReader.plugin("""{"id": "ok", "version": "1", "apiVersion": 99, "entry": "a.ts"}"""))
        assertIs<ManifestResult.Invalid>(ManifestReader.plugin("""{"id": "ok", "version": "1", "apiVersion": 1, "entry": " "}"""))
        assertIs<ManifestResult.Invalid>(ManifestReader.plugin("""{"id": "ok""""))
    }

    @Test
    fun readsAPluginsList() {
        val list = assertIs<ManifestResult.Read<PluginsList>>(ManifestReader.list("""{"plugins": ["todos", "speckit"]}""")).value
        assertEquals(listOf("todos", "speckit"), list.plugins)
    }
}
