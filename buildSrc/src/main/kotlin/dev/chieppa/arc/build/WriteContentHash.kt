package dev.chieppa.arc.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.MessageDigest

/** Writes the SHA-256 of every file in [sources], by relative path and content, as hex to [hashFile] in [outputDir]. */
@CacheableTask
abstract class WriteContentHash : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Input
    abstract val hashFile: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun write() {
        val files = ArrayList<Pair<String, File>>()
        sources.asFileTree.visit { if (!isDirectory) files += relativePath.pathString to file }
        val digest = MessageDigest.getInstance("SHA-256")
        for ((path, file) in files.sortedBy { it.first }) {
            digest.update(path.toByteArray(Charsets.UTF_8))
            digest.update(file.readBytes())
        }
        outputDir.get().asFile.resolve(hashFile.get()).apply { parentFile.mkdirs() }.writeText(digest.digest().joinToString("") { "%02x".format(it) })
    }
}
