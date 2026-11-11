package dev.chieppa.arc.plugins.api

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginTypesTest {
    @Test
    fun theCommittedTypesAreWhatTheKotlinDefinitionsGenerate() {
        val committed = Path.of(System.getProperty("arc.repoRoot")).resolve(TypesGenerator.PATH)
        assertEquals(
            TypesGenerator.generate(),
            Files.readString(committed).replace("\r\n", "\n"),
            "${TypesGenerator.PATH} is stale: run ./gradlew :plugins:generatePluginTypes",
        )
    }

    @Test
    fun everyComponentHasItsPropsInterface() {
        val types = TypesGenerator.generate()
        Components.all.forEach { assertTrue("export interface ${it.propsName}" in types, it.name) }
        assertEquals(Components.all.size, Components.all.map { it.name }.toSet().size)
    }
}
