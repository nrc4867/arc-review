package dev.chieppa.arc.syntax.wasm

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.WasmFunctionHandle
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType

/**
 * The WASI calls the modules import, which only tree-sitter's debug output reaches: every one fails with `ENOSYS`, so
 * a module can reach no file, clock or anything else outside its memory.
 */
internal object WasiStubs {
    private const val MODULE = "wasi_snapshot_preview1"

    /** WASI's errno for a call that is not supported. */
    private const val ENOSYS = 52L

    private val unsupported = WasmFunctionHandle { _, _ -> longArrayOf(ENOSYS) }

    val imports: ImportValues = ImportValues.builder()
        .addFunction(stub("fd_close", ValType.I32))
        .addFunction(stub("fd_seek", ValType.I32, ValType.I64, ValType.I32, ValType.I32))
        .addFunction(stub("fd_write", ValType.I32, ValType.I32, ValType.I32, ValType.I32))
        .build()

    private fun stub(name: String, vararg params: ValType) =
        HostFunction(MODULE, name, FunctionType.of(params.toList(), listOf(ValType.I32)), unsupported)
}
