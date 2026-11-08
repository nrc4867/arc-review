package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.natives.NativeLibraries
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BOOLEAN
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.invoke.MethodHandle

/**
 * Arc's bindings to the tree-sitter C API and to `arc_query_records` (`syntax/native/arc_shim.c`), the functions Arc
 * calls and nothing else. Pointers stay [MemorySegment]s; the classes in this package own them.
 */
internal object TreeSitterApi {
    private val linker = Linker.nativeLinker()
    private val core: SymbolLookup by lazy { NativeLibraries.lookup(NativeLibraries.CORE) }

    private fun function(name: String, descriptor: FunctionDescriptor): MethodHandle =
        linker.downcallHandle(core.find(name).orElseThrow { UnsatisfiedLinkError("No $name in tree-sitter") }, descriptor)

    val parserNew by lazy { function("ts_parser_new", FunctionDescriptor.of(ADDRESS)) }
    val parserDelete by lazy { function("ts_parser_delete", FunctionDescriptor.ofVoid(ADDRESS)) }
    val parserSetLanguage by lazy { function("ts_parser_set_language", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS)) }
    val parserParseStringEncoding by lazy {
        function("ts_parser_parse_string_encoding", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT))
    }
    val treeDelete by lazy { function("ts_tree_delete", FunctionDescriptor.ofVoid(ADDRESS)) }
    val queryNew by lazy { function("ts_query_new", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)) }
    val queryDelete by lazy { function("ts_query_delete", FunctionDescriptor.ofVoid(ADDRESS)) }
    val queryPatternCount by lazy { function("ts_query_pattern_count", FunctionDescriptor.of(JAVA_INT, ADDRESS)) }
    val queryCaptureCount by lazy { function("ts_query_capture_count", FunctionDescriptor.of(JAVA_INT, ADDRESS)) }
    val queryCaptureName by lazy { function("ts_query_capture_name_for_id", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT, ADDRESS)) }
    val queryStringValue by lazy { function("ts_query_string_value_for_id", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT, ADDRESS)) }
    val queryPredicates by lazy { function("ts_query_predicates_for_pattern", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT, ADDRESS)) }
    val queryRecords by lazy { function("arc_query_records", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT)) }

    /** The language a grammar library's entry function [symbol] returns. */
    fun language(library: String, symbol: String): MemorySegment {
        val entry = NativeLibraries.lookup(library).find(symbol).orElseThrow { UnsatisfiedLinkError("No $symbol in $library") }
        return linker.downcallHandle(entry, FunctionDescriptor.of(ADDRESS)).invokeWithArguments() as MemorySegment
    }

    /** UTF-16 little-endian, so a node's byte offset halves to a string index. */
    const val ENCODING_UTF16LE = 1
}
