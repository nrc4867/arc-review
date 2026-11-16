package dev.chieppa.arc.build

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.zip.ZipFile

/**
 * Fails when any of Arc's own jars or class folders in [classpath] holds a native library or the native tree-sitter
 * loader: Arc writes and loads no native code of its own, and calls only the system's libraries through FFM.
 */
abstract class CheckNoNativeCode : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val classpath: ConfigurableFileCollection

    @TaskAction
    fun check() {
        val found = classpath.files.flatMap { file ->
            when {
                file.isDirectory -> file.walkTopDown().filter { it.isFile }.map { it.relativeTo(file).invariantSeparatorsPath }.toList()
                file.isFile -> ZipFile(file).use { zip -> zip.entries().asSequence().map { it.name }.toList() }
                else -> emptyList()
            }.filter(::isNative).map { "${file.name}: $it" }
        }
        if (found.isNotEmpty()) throw GradleException("Native code in Arc's own classpath: ${found.joinToString()}")
    }

    private fun isNative(entry: String): Boolean =
        NATIVE_SUFFIXES.any { entry.endsWith(it, ignoreCase = true) } || entry == NATIVE_LOADER

    private companion object {
        val NATIVE_SUFFIXES = listOf(".dll", ".dylib", ".so", ".jnilib")
        const val NATIVE_LOADER = "dev/chieppa/arc/syntax/natives/NativeLibraries.class"
    }
}
