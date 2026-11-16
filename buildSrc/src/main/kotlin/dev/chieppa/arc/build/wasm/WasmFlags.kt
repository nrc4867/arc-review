package dev.chieppa.arc.build.wasm

/**
 * How each tree-sitter WebAssembly module is compiled and checked. The modules run as JVM bytecode, where the JIT
 * leaves methods over [JIT_METHOD_BYTES] interpreted, so the C keeps functions small: nothing is inlined but functions
 * declared `inline` (the forced-inline header passed with `-include`), and lexers are split into parts of about
 * [LEXER_CHUNK_CHARS] characters ([LexerSplitter]).
 */
object WasmFlags {
    const val TARGET = "wasm32-wasi"

    val COMMON: List<String> = listOf(
        "-mexec-model=reactor",
        "-O2",
        "-fno-inline",
        "-std=c11",
        // wasi-libc hides fdopen, which tree-sitter's debug graphs name, under plain C11.
        "-D_POSIX_C_SOURCE=200809L",
        "-D_DEFAULT_SOURCE",
    )

    /** The functions the engine calls, besides each grammar's `tree_sitter_<name>`. */
    val EXPORTS: List<String> = listOf(
        "malloc",
        "free",
        "ts_parser_new",
        "ts_parser_set_language",
        "ts_parser_parse_string_encoding",
        "ts_tree_delete",
        "ts_query_new",
        "ts_query_pattern_count",
        "ts_query_capture_count",
        "ts_query_capture_name_for_id",
        "ts_query_string_value_for_id",
        "ts_query_predicates_for_pattern",
        "arc_query_records",
    )

    /** The most characters of generated lexer states one lexer part holds. */
    const val LEXER_CHUNK_CHARS = 4000

    /** HotSpot's limit: it never compiles a method with more bytecode than this. */
    const val JIT_METHOD_BYTES = 8000

    /**
     * Functions that may compile past [JIT_METHOD_BYTES]: they run once per query compiled or only for debug output,
     * never per character or node parsed.
     */
    val COLD_FUNCTIONS: Set<String> = setOf(
        "printf_core",
        "ts_query__analyze_patterns",
        "ts_query__perform_analysis",
        "ts_query__parse_pattern",
        "ts_query_new",
    )
}
