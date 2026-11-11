package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * The plugin host every plugin process runs, shipped as a resource and written to [folder] as `host-<hash>.js`, once
 * per build of it, since Node runs files.
 */
class PluginHostScript(private val folder: Path) {
    @Volatile
    private var written: Path? = null

    /** The host's file, written first if needed. Throws [IOException] when this build has no host. */
    suspend fun file(): Path = written ?: withContext(Dispatchers.IO) {
        val bytes = PluginHostScript::class.java.getResourceAsStream(RESOURCE)?.use { it.readAllBytes() }
            ?: throw IOException("This build of Arc has no plugin host")
        val hash = MessageDigest.getInstance(SHA_256).digest(bytes).take(HASH_BYTES).joinToString("") { "%02x".format(it) }
        val target = folder.resolve("host-$hash.js")
        if (!Files.isRegularFile(target)) {
            Files.createDirectories(folder)
            val temp = Files.createTempFile(folder, PREFIX, SUFFIX)
            Files.write(temp, bytes)
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        }
        target.also { written = it }
    }

    private companion object {
        const val RESOURCE = "/dev/chieppa/arc/plugins/host/host.js"
        const val SHA_256 = "SHA-256"
        const val HASH_BYTES = 8
        const val PREFIX = ".host-"
        const val SUFFIX = ".tmp"
    }
}
