package dev.chieppa.arc.syntax.wasm

/**
 * A [Snapshot] of [WasmRuntime] as it stands: the blocks of its memory that differ from a fresh instance of its
 * grammar, joined into runs, and its globals. Memory past a fresh instance's is compared with zeros, which is what
 * growing memory gives.
 */
internal object SnapshotDiff {
    private const val BLOCK_BYTES = 4096

    fun of(grammar: WasmGrammar, runtime: WasmRuntime, prebuilt: Map<String, Long>): Snapshot {
        val memory = runtime.instance.memory()
        val fresh = grammar.instantiate().memory()
        val size = memory.pages() * WasmLimits.PAGE_BYTES.toInt()
        val freshSize = fresh.pages() * WasmLimits.PAGE_BYTES.toInt()
        val zeros = ByteArray(BLOCK_BYTES)
        val runs = ArrayList<Snapshot.Run>()
        var runStart = -1
        var at = 0
        while (at <= size) {
            val differs = at < size && !memory.readBytes(at, BLOCK_BYTES).contentEquals(if (at < freshSize) fresh.readBytes(at, BLOCK_BYTES) else zeros)
            if (differs && runStart < 0) runStart = at
            if (!differs && runStart >= 0) {
                runs += Snapshot.Run(runStart, memory.readBytes(runStart, at - runStart))
                runStart = -1
            }
            at += BLOCK_BYTES
        }
        val instance = runtime.instance
        val globals = LongArray(instance.globalCount()) { instance.global(it).value }
        val state = RuntimeState(runtime.state.language, runtime.state.parser, runtime.state.scratch, prebuilt)
        return Snapshot(memory.pages(), globals, state, runs)
    }
}
