package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.PluginManifest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginTypesTest {
    private val root = Path.of(System.getProperty("arc.repoRoot"))

    @Test
    fun theCommittedTypesAreWhatTheKotlinDefinitionsGenerate() {
        assertEquals(
            TypesGenerator.generate(),
            Files.readString(root.resolve(TypesGenerator.PATH)).replace("\r\n", "\n"),
            "${TypesGenerator.PATH} is stale: run ${TypesGenerator.COMMAND}",
        )
    }

    @Test
    fun theCommittedReferenceIsWhatTheKotlinDefinitionsGenerate() {
        assertEquals(
            ReferenceGenerator.generate(),
            Files.readString(root.resolve(ReferenceGenerator.PATH)).replace("\r\n", "\n"),
            "${ReferenceGenerator.PATH} is stale: run ${TypesGenerator.COMMAND}",
        )
    }

    @Test
    fun everyComponentHasItsPropsInterface() {
        val types = TypesGenerator.generate()
        Components.all.forEach { assertTrue("export interface ${it.propsName}" in types, it.name) }
        assertEquals(Components.all.size, Components.all.map { it.name }.toSet().size)
    }

    @Test
    fun theReferenceDocumentsEveryManifestKeyAndCapability() {
        val manifest = PluginManifest.serializer().descriptor
        val capabilities = Capabilities.serializer().descriptor
        assertEquals((0 until manifest.elementsCount).map(manifest::getElementName).toSet(), ManifestApi.fields.map { it.name }.toSet())
        assertEquals((0 until capabilities.elementsCount).map(capabilities::getElementName).toSet(), ManifestApi.capabilities.map { it.name }.toSet())
    }

    @Test
    fun theReferenceNamesEveryComponentAndAppMember() {
        val reference = ReferenceGenerator.generate()
        Components.all.forEach { assertTrue("### ${it.name}\n" in reference, it.name) }
        AppApi.interfaces.flatMap { it.members }.forEach { assertTrue("| `${it.name}" in reference, it.name) }
    }
}
