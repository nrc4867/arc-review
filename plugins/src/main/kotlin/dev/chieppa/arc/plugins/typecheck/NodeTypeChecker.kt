package dev.chieppa.arc.plugins.typecheck

import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.SystemProcessLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.Path

/**
 * Type checks a plugin in a Node process of its own, started as [spec] says for the plugin's folder and entry: the
 * plugin host run with its check flag, which prints the errors as JSON. It runs beside the plugins' process, never in
 * it, and cancelling the check ends it. A check that cannot run finds nothing.
 */
class NodeTypeChecker(private val spec: suspend (folder: Path, entry: String) -> LaunchSpec) : TypeChecker {
    private val json = Json { ignoreUnknownKeys = true }
    private val errors = ListSerializer(CheckedError.serializer())

    override suspend fun check(folder: Path, entry: String): List<PluginDiagnostic> {
        val output = try {
            run(spec(folder, entry))
        } catch (_: IOException) {
            return emptyList()
        }
        val read = try {
            json.decodeFromString(errors, output)
        } catch (_: SerializationException) {
            return emptyList()
        }
        return read.map { PluginDiagnostic(DiagnosticKind.Type, it.message, it.file.ifEmpty { null }, it.line.takeIf { line -> line > 0 }, it.column) }
    }

    private suspend fun run(spec: LaunchSpec): String = withContext(Dispatchers.IO) {
        val process = SystemProcessLauncher.builder(spec).start()
        try {
            process.outputStream.close()
            coroutineScope {
                val out = async(Dispatchers.IO) { process.inputStream.readAllBytes().toString(Charsets.UTF_8) }
                val err = async(Dispatchers.IO) { process.errorStream.readAllBytes() }
                runInterruptible { process.waitFor() }
                err.await()
                out.await()
            }
        } finally {
            if (process.isAlive) {
                process.descendants().forEach { it.destroyForcibly() }
                process.destroyForcibly()
            }
        }
    }
}
