package dev.chieppa.arc.install

import java.nio.file.Files
import java.nio.file.Path

/** The programs in a Java runtime's home. */
object JavaPrograms {
    /** The program a launcher starts: `javaw.exe` on Windows, which opens no console, `java` elsewhere. */
    fun windowed(home: Path): Path {
        val bin = home.resolve(BIN)
        return listOf(JAVAW, JAVA_EXE).map(bin::resolve).firstOrNull(Files::isRegularFile) ?: bin.resolve(JAVA)
    }

    /** The home of the runtime whose `bin` holds [program]. */
    fun homeOf(program: Path): Path = program.parent.parent

    private const val BIN = "bin"
    private const val JAVAW = "javaw.exe"
    private const val JAVA_EXE = "java.exe"
    private const val JAVA = "java"
}
