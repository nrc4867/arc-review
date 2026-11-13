package dev.chieppa.arc.install

import java.nio.file.Path

/**
 * `launch.args`, the Java argument file every launcher runs (`java @launch.args`): the JVM options and the jar of the
 * current version. Switching versions rewrites only this file.
 */
object LaunchArgs {
    /** The arguments that start [jar]. */
    fun of(jar: Path): List<String> = listOf(InstallConstants.NATIVE_ACCESS, "-D${InstallConstants.LAUNCHED_PROPERTY}=true", JAR_OPTION, jar.toString())

    /** [arguments] as an argument file: one to a line, quoted, backslashes and quotes escaped as Java reads them. */
    fun text(arguments: List<String>): String =
        arguments.joinToString("") { "\"" + it.replace("\\", "\\\\").replace("\"", "\\\"") + "\"\n" }

    /** The argument that hands the file at [file] to `java`. */
    fun reference(file: Path): String = "@$file"

    private const val JAR_OPTION = "-jar"
}
