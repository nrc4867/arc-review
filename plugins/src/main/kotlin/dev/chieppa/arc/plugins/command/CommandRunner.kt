package dev.chieppa.arc.plugins.command

import java.nio.file.Path

/** Runs a program to its end in a folder and gives what it printed. Throws [java.io.IOException] when it cannot start. */
fun interface CommandRunner {
    suspend fun run(command: List<String>, directory: Path): CommandResult
}
