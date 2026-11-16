package dev.chieppa.arc.build.wasm

import org.gradle.api.GradleException

/**
 * Rewrites a tree-sitter grammar's generated `parser.c` so each lexer function (`ts_lex`, `ts_lex_keywords`) becomes
 * several functions of at most about [chunkChars] characters of states each, behind a dispatcher with the original
 * name and signature. Every state-machine part stays as generated; a part hands a state outside its range back to the
 * dispatcher after advancing the lexer, and the dispatcher calls the part holding it. Compiled through WebAssembly to
 * JVM bytecode, the parts stay small enough for the JIT, where one lexer function would not.
 */
object LexerSplitter {
    private val HEAD = Regex(
        "static bool (\\w+)\\(TSLexer \\*lexer, TSStateId state\\) \\{\\n" +
            " {2}START_LEXER\\(\\);\\n {2}eof = lexer->eof\\(lexer\\);\\n {2}switch \\(state\\) \\{\\n",
    )
    private const val TAIL = "    default:\n      return false;\n  }\n}\n"
    private val CASE = Regex("^ {4}case (\\d+):\\n", RegexOption.MULTILINE)

    /** [source] with every generated lexer function split; fails when a lexer is not in the generated shape. */
    fun split(source: String, chunkChars: Int): String {
        val out = StringBuilder()
        var at = 0
        while (true) {
            val head = HEAD.find(source, at) ?: break
            val name = head.groupValues[1]
            val end = source.indexOf(TAIL, head.range.last + 1)
            if (end < 0) throw GradleException("$name has no generated default case")
            out.append(source, at, head.range.first)
            out.append(function(name, source.substring(head.range.last + 1, end), chunkChars))
            at = end + TAIL.length
        }
        if (at == 0) throw GradleException("No generated lexer function found")
        out.append(source, at, source.length)
        return out.toString()
    }

    /** One case of the lexer's switch: its state and where its text starts in the switch body. */
    private class Case(val state: Int, val start: Int)

    private fun function(name: String, body: String, chunkChars: Int): String {
        val cases = CASE.findAll(body).map { Case(it.groupValues[1].toInt(), it.range.first) }.toList()
        if (cases.isEmpty() || cases.first().start != 0) throw GradleException("$name is not a switch of cases")
        if (cases.zipWithNext().any { (a, b) -> b.state <= a.state }) throw GradleException("$name's states are out of order")
        val parts = chunks(cases, body.length, chunkChars)
        return buildString {
            append("#undef END_STATE\n#define END_STATE() { *result_io = result; return $DONE; }\n")
            parts.forEachIndexed { index, part ->
                val from = cases[part.first]
                val until = cases.getOrNull(part.last + 1)?.start ?: body.length
                append(part(name, index, from.state, cases[part.last].state, body.substring(from.start, until)))
            }
            append("#undef END_STATE\n#define END_STATE() return result;\n")
            append(dispatcher(name, parts.map { cases[it.last].state }))
        }
    }

    /** Consecutive runs of [cases] whose text stays within [chunkChars], each at least one case. */
    private fun chunks(cases: List<Case>, bodyLength: Int, chunkChars: Int): List<IntRange> {
        val parts = ArrayList<IntRange>()
        var from = 0
        for (i in cases.indices) {
            val end = cases.getOrNull(i + 1)?.start ?: bodyLength
            if (i > from && end - cases[from].start > chunkChars) {
                parts += from until i
                from = i
            }
        }
        parts += from until cases.size
        return parts
    }

    // The generated cases go in verbatim, outside any template processing.
    private fun part(name: String, index: Int, low: Int, high: Int, cases: String): String = """
        |__attribute__((noinline)) static int32_t ${name}_part$index(TSLexer *lexer, int32_t state, bool *result_io) {
        |  bool result = *result_io;
        |  bool skip = false;
        |  bool eof = false;
        |  int32_t lookahead;
        |  goto start;
        |  next_state:
        |  lexer->advance(lexer, skip);
        |  if (state < $low || state > $high) {
        |    *result_io = result;
        |    return state;
        |  }
        |  start:
        |  skip = false;
        |  lookahead = lexer->lookahead;
        |  eof = lexer->eof(lexer);
        |  (void)eof;
        |  switch (state) {
        |
    """.trimMargin() + cases + """
        |    default:
        |      *result_io = false;
        |      return $DONE;
        |  }
        |}
        |
        |
    """.trimMargin()

    private fun dispatcher(name: String, highs: List<Int>): String = buildString {
        append("static bool $name(TSLexer *lexer, TSStateId state) {\n")
        append("  bool result = false;\n  int32_t next = state;\n  while (next >= 0) {\n")
        highs.forEachIndexed { index, high ->
            append(if (index == 0) "    if" else "    else if")
            append(" (next <= $high) next = ${name}_part$index(lexer, next, &result);\n")
        }
        append("    else return false;\n  }\n  return result;\n}\n")
    }

    /** What a part returns once the lexer is done: any negative number, since states are never negative. */
    private const val DONE = "-1"
}
