package dev.chieppa.arc.syntax.wasm

import com.dylibso.chicory.runtime.Instance

/**
 * Makes one instance of a module the same as another: its memory grown to the same size and copied, and its
 * globals (the C stack pointer among them) set alike. Everything the C code made in the first, such as compiled
 * queries and the parser, is then at the same addresses in the second.
 */
internal object InstanceCopy {
    fun copy(from: Instance, to: Instance) {
        val source = from.memory()
        val target = to.memory()
        val missing = source.pages() - target.pages()
        if (missing > 0) target.grow(missing)
        val bytes = source.pages().toLong() * WasmLimits.PAGE_BYTES
        var at = 0L
        while (at < bytes) {
            val length = minOf(WasmLimits.COPY_CHUNK_BYTES.toLong(), bytes - at).toInt()
            target.write(at.toInt(), source.readBytes(at.toInt(), length))
            at += length
        }
        for (i in 0 until from.globalCount()) to.global(i).setValue(from.global(i).value)
    }
}
