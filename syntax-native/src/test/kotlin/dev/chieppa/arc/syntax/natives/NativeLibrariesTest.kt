package dev.chieppa.arc.syntax.natives

import kotlin.test.Test
import kotlin.test.assertTrue

class NativeLibrariesTest {
    @Test
    fun thisMachinesLibrariesAreBundledAndLoad() {
        assertTrue(NativeLibraries.available, "no libraries bundled for ${NativePlatform.current}")
        assertTrue(NativeLibraries.lookup(NativeLibraries.CORE).find("arc_query_records").isPresent)
        for (grammar in GRAMMARS) {
            val library = SyntaxLanguages.library(grammar)
            assertTrue(NativeLibraries.lookup(library).find(SyntaxLanguages.symbol(grammar)).isPresent, library)
        }
    }

    private companion object {
        val GRAMMARS = listOf("java", "kotlin", "javascript", "typescript", "tsx", "mssql")
    }
}
