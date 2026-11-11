package dev.chieppa.arc.plugins.starter

import dev.chieppa.arc.plugins.host.HostResources
import dev.chieppa.arc.plugins.manifest.ApiVersion
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Makes a new plugin's folder: a manifest, a tsconfig, one entry with a panel and an action, a CLAUDE.md on the API and
 * the development loop, and under `arc/` the SDK's types and the API reference of this build of Arc, so its author
 * works from what this Arc offers without fetching anything.
 */
object PluginStarter {
    private const val TEMPLATES = "/dev/chieppa/arc/plugins/starter/"
    private const val DOCS = "docs/"
    private const val ID = "{{id}}"
    private const val NAME = "{{name}}"
    private const val API = "{{apiVersion}}"

    /** Makes the plugin [typed] names in a folder of its id under [parent], and gives that folder. */
    fun create(parent: Path, typed: String): Path {
        val name = StarterName.of(typed) ?: throw StarterException(StarterProblem.BadName, typed)
        val folder = parent.toAbsolutePath().normalize().resolve(name.id)
        if (Files.isDirectory(folder) && Files.list(folder).use { it.findAny().isPresent }) throw StarterException(StarterProblem.FolderTaken, folder.toString())
        try {
            StarterFiles.TEMPLATES.forEach { (path, template) -> write(folder.resolve(path), fill(template(template), name).toByteArray(Charsets.UTF_8)) }
            HostResources.names().filter { file -> StarterFiles.COPIED.any(file::startsWith) }.forEach { file ->
                write(folder.resolve(StarterFiles.ARC).resolve(file.removePrefix(DOCS)), HostResources.read(file))
            }
        } catch (e: IOException) {
            throw StarterException(StarterProblem.NotWritten, e.message ?: e.toString())
        }
        return folder
    }

    private fun fill(template: String, name: StarterName): String =
        template.replace(ID, name.id).replace(NAME, name.title).replace(API, ApiVersion.CURRENT.toString())

    private fun template(name: String): String =
        PluginStarter::class.java.getResourceAsStream("$TEMPLATES$name")?.use { it.readAllBytes().toString(Charsets.UTF_8) } ?: throw IOException(name)

    private fun write(file: Path, bytes: ByteArray) {
        file.parent?.let(Files::createDirectories)
        Files.write(file, bytes)
    }
}
