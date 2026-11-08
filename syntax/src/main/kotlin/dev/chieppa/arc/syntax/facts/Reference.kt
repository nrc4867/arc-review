package dev.chieppa.arc.syntax.facts

/**
 * A use of [name] at string indices [start] until [end], on [line] (from 1) at [column] (from 0). [arity] counts a
 * call's arguments; [qualified] says the name follows a `.`, so it is looked up on a receiver; [schema] is a SQL
 * name's written schema.
 */
data class Reference(
    val name: String,
    val kind: RefKind,
    val start: Int,
    val end: Int,
    val line: Int,
    val column: Int,
    val arity: Int? = null,
    val qualified: Boolean = false,
    val schema: String? = null,
)
