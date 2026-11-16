package dev.chieppa.arc.syntax.wasm

/** Sizes the WebAssembly engine works within. */
internal object WasmLimits {
    /** Capture records a module's result buffer holds at first; it grows to the largest run. */
    const val INITIAL_RECORDS = 1 shl 12

    /**
     * An instance whose memory has grown past this after a parse is dropped rather than kept for the next one, since
     * WebAssembly memory never shrinks.
     */
    const val POOLED_MEMORY_BYTES = 64L shl 20

    /** Bytes copied at a time when one instance's memory is copied into another. */
    const val COPY_CHUNK_BYTES = 1 shl 20

    /** A WebAssembly memory page. */
    const val PAGE_BYTES = 1L shl 16

    /** tree-sitter's `TSInputEncodingUTF16LE`. */
    const val ENCODING_UTF16LE = 1L
}
