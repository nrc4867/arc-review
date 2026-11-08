package dev.chieppa.arc.syntax.natives

import dev.chieppa.arc.syntax.grammar.Grammars
import kotlin.test.Test
import kotlin.test.assertTrue

class NativeLibrariesTest {
    @Test
    fun thisMachinesLibrariesAreBundledAndLoad() {
        assertTrue(NativeLibraries.available, "no libraries bundled for ${NativePlatform.current}")
        assertTrue(NativeLibraries.lookup(NativeLibraries.CORE).find("arc_query_records").isPresent)
        for (grammar in Grammars.all) {
            assertTrue(NativeLibraries.lookup(grammar.library).find(grammar.symbol).isPresent, grammar.library)
        }
    }
}
