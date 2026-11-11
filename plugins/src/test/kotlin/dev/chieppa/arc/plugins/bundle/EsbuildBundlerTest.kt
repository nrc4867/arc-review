package dev.chieppa.arc.plugins.bundle

import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EsbuildBundlerTest {
    private val out = createTempDirectory("arc-bundle").resolve("todos/bundle.js")
    private val esbuildOutput = """
        ✘ [ERROR] Expected ";" but found "line"

            src/index.tsx:14:5:
              14 │ let line
                 ╵      ^

        ✘ [ERROR] Could not resolve "left-pad"

        2 errors
    """.trimIndent()

    @Test
    fun readsEsbuildsErrorsWithTheirPlaces() {
        assertEquals(
            listOf(
                PluginDiagnostic(DiagnosticKind.Transpile, "Expected \";\" but found \"line\"", "src/index.tsx", 14, 5),
                PluginDiagnostic(DiagnosticKind.Transpile, "Could not resolve \"left-pad\""),
            ),
            EsbuildMessages.errors(esbuildOutput),
        )
    }

    @Test
    fun bundlesWithReactAndTheSdkLeftToTheHost() = runTest {
        var ran: List<String> = emptyList()
        val bundler = EsbuildBundler({ Path.of("esbuild") }) { command, _ ->
            ran = command
            CommandResult(0, "", "")
        }
        assertEquals(BundleResult.Bundled(out), bundler.bundle(Path.of("."), "src/index.tsx", out))
        assertEquals(listOf("esbuild", "src/index.tsx", "--bundle"), ran.take(3))
        assertTrue("--external:react" in ran && "--external:@review/plugin" in ran && "--outfile=$out" in ran)
    }

    @Test
    fun aFailedBundleOrAMissingEsbuildIsDiagnosed() = runTest {
        val failing = EsbuildBundler({ Path.of("esbuild") }) { _, _ -> CommandResult(1, "", esbuildOutput) }
        assertEquals(2, assertIs<BundleResult.Failed>(failing.bundle(Path.of("."), "src/index.tsx", out)).diagnostics.size)
        val missing = EsbuildBundler({ throw IOException("offline") }) { _, _ -> error("not run") }
        assertEquals("offline", assertIs<BundleResult.Failed>(missing.bundle(Path.of("."), "src/index.tsx", out)).diagnostics.single().message)
    }
}
