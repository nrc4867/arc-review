package dev.chieppa.arc.build.wasm

import com.dylibso.chicory.build.time.compiler.Config
import com.dylibso.chicory.build.time.compiler.Generator
import com.dylibso.chicory.compiler.InterpreterFallback
import com.dylibso.chicory.wasm.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Compiles every module in [wasmDir] to JVM classes with Chicory's build-time compiler, into [outputDir] under
 * [packageName]: `<Name>Machine` and its helper classes, and `<Name>.meta`, the module without its function bodies,
 * which Chicory reads for its memory, data and tables. Every function must compile: the build fails rather than leave
 * one to Chicory's interpreter, and fails on a hot function the JIT would not compile ([MethodSizeCheck]).
 */
@CacheableTask
abstract class CompileWasmClasses : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val wasmDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val files: FileSystemOperations

    @TaskAction
    fun compile() {
        val out = outputDir.get().asFile
        files.delete { delete(out) }
        out.mkdirs()
        val modules = wasmDir.get().asFile.listFiles { file -> file.name.endsWith(WASM) }.orEmpty().sortedBy { it.name }
        for (wasm in modules) {
            val name = WasmClassNames.of(wasm.name.removeSuffix(WASM))
            val config = Config.builder()
                .withWasmFile(wasm.toPath())
                .withName("${packageName.get()}.$name")
                .withTargetClassFolder(out.toPath())
                .withTargetWasmFolder(out.toPath())
                .withInterpreterFallback(InterpreterFallback.FAIL)
                .build()
            val generator = Generator(config)
            generator.generateMetaWasm(generator.generateResources())
            val folder = out.resolve(packageName.get().replace('.', '/'))
            val classes = folder.listFiles { file -> file.name.startsWith(name + MACHINE) && file.name.endsWith(CLASS) }.orEmpty()
            MethodSizeCheck.check(Parser.parse(wasm.toPath()), wasm.name, classes.toList())
        }
    }

    private companion object {
        const val WASM = ".wasm"
        const val MACHINE = "Machine"
        const val CLASS = ".class"
    }
}
