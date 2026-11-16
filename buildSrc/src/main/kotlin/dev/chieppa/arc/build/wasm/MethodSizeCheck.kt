package dev.chieppa.arc.build.wasm

import com.dylibso.chicory.wasm.WasmModule
import org.gradle.api.GradleException
import java.io.File
import java.lang.classfile.Attributes
import java.lang.classfile.ClassFile

/**
 * Fails when a class compiled from a WebAssembly module holds a method the JIT would leave interpreted (over
 * [WasmFlags.JIT_METHOD_BYTES] of bytecode) for a function not in [WasmFlags.COLD_FUNCTIONS]. Chicory names each
 * function's method `func_<index>`; the module's name section gives the C function.
 */
object MethodSizeCheck {
    private val FUNCTION = Regex("func_(\\d+)")

    fun check(module: WasmModule, moduleName: String, classes: List<File>) {
        val names = module.nameSection()
        val tooLarge = classes.flatMap { file ->
            ClassFile.of().parse(file.readBytes()).methods().mapNotNull { method ->
                val index = FUNCTION.matchEntire(method.methodName().stringValue())?.groupValues?.get(1)?.toInt()
                val bytes = method.findAttribute(Attributes.code()).map { it.codeLength() }.orElse(0)
                if (index == null || bytes <= WasmFlags.JIT_METHOD_BYTES) return@mapNotNull null
                val name = names?.nameOfFunction(index) ?: "function $index"
                if (name in WasmFlags.COLD_FUNCTIONS) null else "$name ($bytes bytes)"
            }
        }
        if (tooLarge.isNotEmpty()) {
            throw GradleException(
                "$moduleName compiles to methods over ${WasmFlags.JIT_METHOD_BYTES} bytes of bytecode, which the JIT " +
                    "leaves interpreted: ${tooLarge.joinToString()}",
            )
        }
    }
}
