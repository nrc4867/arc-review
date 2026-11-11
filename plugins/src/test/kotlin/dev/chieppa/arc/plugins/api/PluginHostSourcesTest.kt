package dev.chieppa.arc.plugins.api

import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.PatchOp
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

/** The plugin host's TypeScript names the protocol's methods as Kotlin's [Methods] does. */
class PluginHostSourcesTest {
    private val protocol = Files.readString(Path.of(System.getProperty("arc.repoRoot")).resolve("plugin-sdk/src/host/protocol.ts"))

    @Test
    fun everyMethodIsNamedTheSameOnBothSides() {
        val kotlin = Methods::class.java.declaredFields.filter { it.type == String::class.java }.map { it.get(null) as String }.toSet()
        val typescript = Regex("""^\s+\w+: "([a-z.]+)",$""", RegexOption.MULTILINE).findAll(protocol.substringBefore("} as const")).map { it.groupValues[1] }.toSet()
        assertEquals(kotlin, typescript)
    }

    @Test
    fun theHandlerMarkAndRootAreTheSame() {
        assertEquals(true, "HANDLER = \"${PatchOp.HANDLER}\"" in protocol)
        assertEquals(true, "ROOT = ${PatchOp.ROOT};" in protocol)
    }
}
