package dev.chieppa.arc.build.natives

import org.gradle.api.GradleException
import java.io.File

/** Reads grammars.tsv: tab-separated name, repository, commit and source folder; blank lines and `#` lines skipped. */
object GrammarPins {
    private const val COLUMNS = 4

    fun read(file: File): List<GrammarPin> = file.readLines()
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .map { line ->
            val cells = line.split('\t')
            if (cells.size != COLUMNS) throw GradleException("${file.name}: expected $COLUMNS tab-separated columns in '$line'")
            GrammarPin(cells[0], cells[1], cells[2], cells[3])
        }
}
