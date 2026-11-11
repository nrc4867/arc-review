package dev.chieppa.arc.plugins.api

import java.nio.file.Files
import java.nio.file.Path

/** Writes the generated plugin API types to the first argument's path and its Markdown reference to the second's. */
fun main(args: Array<String>) {
    val (types, reference) = args.map(Path::of)
    write(types, TypesGenerator.generate())
    write(reference, ReferenceGenerator.generate())
}

private fun write(target: Path, text: String) {
    target.parent?.let(Files::createDirectories)
    Files.writeString(target, text)
}
