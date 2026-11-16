package dev.chieppa.arc.syntax.engine

import java.util.ServiceLoader

/** The engine on the classpath, found through [ServiceLoader]: `syntax-wasm`, or `syntax-native` when chosen. */
object SyntaxEngines {
    private val found: SyntaxEngine? by lazy { ServiceLoader.load(SyntaxEngine::class.java).firstOrNull() }

    /** Whether an engine is on the classpath. */
    val available: Boolean get() = found != null

    val current: SyntaxEngine get() = found ?: error("No SyntaxEngine on the classpath")
}
