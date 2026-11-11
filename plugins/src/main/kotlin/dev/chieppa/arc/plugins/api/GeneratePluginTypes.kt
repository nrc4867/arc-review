package dev.chieppa.arc.plugins.api

import java.nio.file.Files
import java.nio.file.Path

/** Writes the generated plugin API types to the path given as the first argument. */
fun main(args: Array<String>) {
    val target = Path.of(args.single())
    target.parent?.let(Files::createDirectories)
    Files.writeString(target, TypesGenerator.generate())
}
