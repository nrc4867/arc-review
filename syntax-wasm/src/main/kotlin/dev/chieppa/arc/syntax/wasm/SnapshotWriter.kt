package dev.chieppa.arc.syntax.wasm

import java.io.DataOutputStream
import java.io.File

/**
 * Writes each grammar's [Snapshot] at build time (`:syntax-wasm:writeSnapshots`): a fresh instance initialised, with
 * every query file of the grammar's folder and of the queries folder itself compiled in. Arguments: the queries
 * folder, the folder to write `<grammar>.snapshot` files into.
 */
internal object SnapshotWriter {
    private const val QUERY_SUFFIX = ".scm"

    @JvmStatic
    fun main(args: Array<String>) {
        val queries = File(args[0])
        val out = File(args[1]).also { it.mkdirs() }
        for (id in WasmGrammars.ids.sorted()) {
            val grammar = WasmGrammars.of(id)
            val runtime = WasmRuntime.start(grammar)
            val sources = (files(queries.resolve(id)) + files(queries)).map(File::readText)
            val prebuilt = sources.associate { QueryHash.of(it) to runtime.compile(it) }
            val snapshot = SnapshotDiff.of(grammar, runtime, prebuilt)
            DataOutputStream(out.resolve("$id${Snapshot.SUFFIX}").outputStream().buffered()).use(snapshot::write)
        }
    }

    private fun files(folder: File): List<File> =
        folder.listFiles { file -> file.isFile && file.name.endsWith(QUERY_SUFFIX) }.orEmpty().sortedBy { it.name }
}
