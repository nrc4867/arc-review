package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * The plugin host the plugins' Node process runs, and the TypeScript compiler its main thread converts their code
 * with, shipped as resources and written to [folder] as `host-<hash>/`, once per build of them, since Node runs files.
 */
class PluginHostScript(private val folder: Path) {
    @Volatile
    private var written: HostFiles? = null

    /** The host's files, written first if needed. Throws [IOException] when this build has no host. */
    suspend fun files(): HostFiles = written ?: withContext(Dispatchers.IO) {
        val contents = FILES.associateWith { name ->
            PluginHostScript::class.java.getResourceAsStream("$RESOURCES/$name")?.use { it.readAllBytes() } ?: throw IOException("$MISSING $name")
        }
        val digest = MessageDigest.getInstance(SHA_256)
        contents.values.forEach(digest::update)
        val hash = digest.digest().take(HASH_BYTES).joinToString("") { "%02x".format(it) }
        val target = folder.resolve("$PREFIX$hash")
        if (!Files.isDirectory(target)) {
            Files.createDirectories(folder)
            val temp = Files.createTempDirectory(folder, TEMP_PREFIX)
            contents.forEach { (name, bytes) -> Files.write(temp.resolve(name), bytes) }
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: IOException) {
                temp.toFile().deleteRecursively()
                if (!Files.isDirectory(target)) throw e
            }
        }
        HostFiles(target, target.resolve(SCRIPT)).also { written = it }
    }

    private companion object {
        const val RESOURCES = "/dev/chieppa/arc/plugins/host"
        const val SCRIPT = "host.js"
        const val TYPESCRIPT = "typescript.js"
        val FILES = listOf(SCRIPT, TYPESCRIPT)
        const val MISSING = "This build of Arc has no plugin host file"
        const val SHA_256 = "SHA-256"
        const val HASH_BYTES = 8
        const val PREFIX = "host-"
        const val TEMP_PREFIX = ".host-"
    }
}
