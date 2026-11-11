package dev.chieppa.arc.plugins.host

import java.io.IOException

/** The plugin host's files shipped as resources of this module, named in the bundle's index. */
object HostResources {
    private const val RESOURCES = "/dev/chieppa/arc/plugins/host"
    private const val INDEX = "files.txt"
    private const val MISSING = "This build of Arc has no plugin host file"

    /** The index's own name, which is shipped too. */
    const val INDEX_FILE = INDEX

    /** The name of every file the index lists, relative to the host's folder. Throws [IOException] when there is none. */
    fun names(): List<String> = read(INDEX).toString(Charsets.UTF_8).lines().filter { it.isNotBlank() }

    /** The bytes of the host file [name]. Throws [IOException] when this build has none. */
    fun read(name: String): ByteArray =
        HostResources::class.java.getResourceAsStream("$RESOURCES/$name")?.use { it.readAllBytes() } ?: throw IOException("$MISSING $name")
}
