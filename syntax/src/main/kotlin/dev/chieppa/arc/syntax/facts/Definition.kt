package dev.chieppa.arc.syntax.facts

/**
 * A definition of [name] in a file. [label] is its name as written, [name] the key it is looked up by (a SQL name's
 * lower-case last part). Positions are string indices; [line] counts from 1 and [column] from 0. [spanStart] and
 * [spanEnd] cover the whole definition. [parameters] are a callable's parameter names, null for anything else; [type]
 * is its declared or return type as written. [parent] is the index of the innermost definition around it, -1 at the
 * top, and [container] and [containerKind] that definition's name and kind (or a SQL name's schema).
 */
data class Definition(
    val name: String,
    val label: String,
    val kind: SymbolKind,
    val nameStart: Int,
    val nameEnd: Int,
    val line: Int,
    val column: Int,
    val spanStart: Int,
    val spanEnd: Int,
    val parameters: List<String>? = null,
    val type: String? = null,
    val parent: Int = -1,
    val container: String? = null,
    val containerKind: SymbolKind? = null,
) {
    /** How many parameters a callable takes, or null. */
    val arity: Int? get() = parameters?.size

    /** Defined inside a function, so seen only there. */
    val isLocal: Boolean get() = containerKind?.isCallable == true
}
