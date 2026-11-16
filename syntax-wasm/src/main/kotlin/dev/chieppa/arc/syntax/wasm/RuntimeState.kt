package dev.chieppa.arc.syntax.wasm

/**
 * Addresses in a grammar instance's memory that every copy of it shares: the language, the parser, two ints the C
 * API writes results into, and the queries the build compiled, by [QueryHash] of their source.
 */
internal class RuntimeState(val language: Long, val parser: Long, val scratch: Long, val prebuilt: Map<String, Long>)
