package dev.chieppa.arc.build

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails when a test source waits on the wall clock or uses reflection to reach past visibility, naming each file and
 * line. A test drives time through its own dispatcher or an await hook instead.
 */
abstract class CheckTestSources : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @TaskAction
    fun run() {
        val found = sources.files.sortedBy { it.path }.flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> FORBIDDEN.any { it.containsMatchIn(line) } }
                .map { (index, line) -> "${file.path}:${index + 1}: ${line.trim()}" }
        }
        if (found.isNotEmpty()) {
            throw GradleException("Tests must not wait on the clock or widen visibility by reflection:\n" + found.joinToString("\n"))
        }
    }

    private companion object {
        val FORBIDDEN = listOf(
            Regex("""\bThread\.sleep\s*\("""),
            Regex("""\bdelay\s*\("""),
            Regex("""\bwaitUntil\s*[({]"""),
            Regex("""\bsetAccessible\b"""),
            Regex("""\bisAccessible\s*=\s*true\b"""),
        )
    }
}
