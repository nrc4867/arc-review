package dev.chieppa.arc.plugins.bundle

import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Turns a plugin's TypeScript and JSX into one CommonJS file with esbuild, the program [esbuild] gives, run through
 * [runner]. The plugin's npm packages are bundled in; React and Arc's SDK stay outside it, given by the plugin host, so
 * every plugin renders through the host's one React.
 */
class EsbuildBundler(private val esbuild: suspend () -> Path, private val runner: CommandRunner = ProcessCommandRunner) {
    /** Bundles [entry], a path relative to [folder], into [out]. */
    suspend fun bundle(folder: Path, entry: String, out: Path): BundleResult {
        val program = try {
            esbuild()
        } catch (e: IOException) {
            return failed(e.message ?: e.toString())
        }
        out.parent?.let(Files::createDirectories)
        val result = try {
            runner.run(command(program, entry, out), folder)
        } catch (e: IOException) {
            return failed(e.message ?: e.toString())
        }
        if (result.code == 0) return BundleResult.Bundled(out)
        return BundleResult.Failed(EsbuildMessages.errors(result.error).ifEmpty { listOf(diagnostic(result.error.trim())) })
    }

    /** The esbuild command line for [entry]. */
    fun command(program: Path, entry: String, out: Path): List<String> =
        listOf(program.toString(), entry) + FLAGS + EXTERNAL.map { "$EXTERNAL_FLAG$it" } + "$OUT_FLAG$out"

    private fun failed(message: String) = BundleResult.Failed(listOf(diagnostic(message)))

    private fun diagnostic(message: String) = PluginDiagnostic(DiagnosticKind.Transpile, message)

    companion object {
        /** The modules the plugin host gives every plugin. */
        val EXTERNAL = listOf("react", "react/jsx-runtime", "react/jsx-dev-runtime", "@review/plugin")
        private const val EXTERNAL_FLAG = "--external:"
        private const val OUT_FLAG = "--outfile="
        private val FLAGS = listOf(
            "--bundle",
            "--platform=node",
            "--format=cjs",
            "--target=node22",
            "--jsx=automatic",
            "--sourcemap=inline",
            "--log-level=error",
            "--color=false",
        )
    }
}
