package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.FilesReadParams
import dev.chieppa.arc.plugins.protocol.FilesReadResult
import dev.chieppa.arc.plugins.protocol.FilesWriteParams
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** `app.files` for one plugin in [folder]: reads and writes only under the folders its [capabilities] name. */
internal class FileCalls(private val folder: () -> Path, private val capabilities: () -> Capabilities) {
    suspend fun read(params: FilesReadParams): FilesReadResult = withContext(Dispatchers.IO) {
        val file = PluginPaths.resolve(folder(), capabilities().files.read, params.path) ?: throw denied(params.path)
        try {
            if (Files.size(file) > MAX_BYTES) throw failed("$TOO_LARGE ${params.path}")
            FilesReadResult(Files.readString(file))
        } catch (e: IOException) {
            throw failed(e.message ?: e.toString())
        }
    }

    suspend fun write(params: FilesWriteParams) = withContext(Dispatchers.IO) {
        val file = PluginPaths.resolve(folder(), capabilities().files.write, params.path) ?: throw denied(params.path)
        try {
            file.parent?.let(Files::createDirectories)
            Files.writeString(file, params.text)
        } catch (e: IOException) {
            throw failed(e.message ?: e.toString())
        }
        Unit
    }

    private fun denied(path: String) = RpcException(RpcError(ErrorCodes.DENIED, "$OUTSIDE $path"))

    private fun failed(message: String) = RpcException(RpcError(ErrorCodes.FAILED, message))

    private companion object {
        const val MAX_BYTES = 8L * 1024 * 1024
        const val OUTSIDE = "Not under a folder the plugin's manifest names:"
        const val TOO_LARGE = "Larger than a plugin may read:"
    }
}
