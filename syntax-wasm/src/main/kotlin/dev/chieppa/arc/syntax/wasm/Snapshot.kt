package dev.chieppa.arc.syntax.wasm

import com.dylibso.chicory.runtime.Instance
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * A grammar instance as the build left it after initialising it and compiling Arc's queries: its memory's size, the
 * runs of memory that differ from a fresh instance's, its globals and its [state]. Applied to a fresh instance, it
 * gives one whose queries need no compiling, which in WebAssembly takes seconds per grammar.
 */
internal class Snapshot(
    val pages: Int,
    val globals: LongArray,
    val state: RuntimeState,
    val runs: List<Run>,
) {
    /** [bytes] at [offset] in memory. */
    class Run(val offset: Int, val bytes: ByteArray)

    fun applyTo(instance: Instance) {
        val memory = instance.memory()
        val missing = pages - memory.pages()
        if (missing > 0) memory.grow(missing)
        for (run in runs) memory.write(run.offset, run.bytes)
        globals.forEachIndexed { i, value -> instance.global(i).setValue(value) }
    }

    fun write(out: DataOutputStream) {
        out.writeInt(MAGIC)
        out.writeInt(pages)
        out.writeInt(globals.size)
        globals.forEach(out::writeLong)
        out.writeLong(state.language)
        out.writeLong(state.parser)
        out.writeLong(state.scratch)
        out.writeInt(state.prebuilt.size)
        for ((hash, pointer) in state.prebuilt.toSortedMap()) {
            out.writeUTF(hash)
            out.writeLong(pointer)
        }
        out.writeInt(runs.size)
        for (run in runs) {
            out.writeInt(run.offset)
            out.writeInt(run.bytes.size)
            out.write(run.bytes)
        }
    }

    companion object {
        /** The format's mark and version: `ARC` and 1. */
        private const val MAGIC = 0x41524301

        fun read(input: DataInputStream): Snapshot {
            check(input.readInt() == MAGIC) { "Not a grammar snapshot of this version" }
            val pages = input.readInt()
            val globals = LongArray(input.readInt()) { input.readLong() }
            val language = input.readLong()
            val parser = input.readLong()
            val scratch = input.readLong()
            val queries = (0 until input.readInt()).associate { input.readUTF() to input.readLong() }
            val runs = (0 until input.readInt()).map {
                val offset = input.readInt()
                Run(offset, ByteArray(input.readInt()).also(input::readFully))
            }
            return Snapshot(pages, globals, RuntimeState(language, parser, scratch, queries), runs)
        }

        /** [grammar]'s snapshot, bundled by `:syntax-wasm:writeSnapshots`, or null when the build made none. */
        fun bundled(grammar: String): Snapshot? =
            Snapshot::class.java.getResourceAsStream("$ROOT/$grammar$SUFFIX")?.let { stream ->
                DataInputStream(stream.buffered()).use(::read)
            }

        const val ROOT = "/dev/chieppa/arc/syntax/wasm/snapshots"
        const val SUFFIX = ".snapshot"
    }
}
