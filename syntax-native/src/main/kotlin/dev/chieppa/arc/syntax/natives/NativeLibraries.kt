package dev.chieppa.arc.syntax.natives

import java.lang.foreign.Arena
import java.lang.foreign.SymbolLookup
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * The tree-sitter core and grammar libraries for this machine, bundled under `natives/<platform>/`. Each is unpacked
 * once into [folder] under a name holding a hash of its content, so a newer build never overwrites a library another
 * running copy of Arc has loaded, then loaded for the life of the process.
 */
object NativeLibraries {
    private const val RESOURCE_ROOT = "/dev/chieppa/arc/syntax/natives"
    private const val HASH_CHARS = 16

    /** Where libraries are unpacked. */
    val folder: Path by lazy {
        Path.of(System.getProperty("java.io.tmpdir"), "arc-syntax", NativePlatform.current).also { Files.createDirectories(it) }
    }

    private val lookups = ConcurrentHashMap<String, SymbolLookup>()

    /** Whether the libraries for this machine are bundled. */
    val available: Boolean by lazy { NativeLibraries::class.java.getResource(resource(CORE)) != null }

    /** The symbols of library [name], such as `tree-sitter` or `tree-sitter-kotlin`. */
    fun lookup(name: String): SymbolLookup = lookups.computeIfAbsent(name) {
        SymbolLookup.libraryLookup(unpack(name), Arena.global())
    }

    private fun resource(name: String) = "$RESOURCE_ROOT/${NativePlatform.current}/${System.mapLibraryName(name)}"

    private fun unpack(name: String): Path {
        val resource = resource(name)
        val bytes = NativeLibraries::class.java.getResourceAsStream(resource)?.use { it.readAllBytes() }
            ?: throw UnsatisfiedLinkError("No $resource bundled for ${NativePlatform.current}")
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }.take(HASH_CHARS)
        val target = folder.resolve("$hash-${System.mapLibraryName(name)}")
        if (!Files.exists(target) || Files.size(target) != bytes.size.toLong()) {
            val temp = Files.createTempFile(folder, name, ".part")
            Files.write(temp, bytes)
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        }
        return target
    }

    /** The core library's name. */
    const val CORE = "tree-sitter"
}
