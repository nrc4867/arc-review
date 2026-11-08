package dev.chieppa.arc.syntax.facts

/** What a definition is, from its query capture `@definition.<tag>`. */
enum class SymbolKind(val tag: String) {
    Class("class"),
    Interface("interface"),
    Object("object"),
    Enum("enum"),
    Type("type"),
    Function("function"),
    Method("method"),
    Constructor("constructor"),
    Property("property"),
    Constant("constant"),
    Module("module"),
    Procedure("procedure"),
    Table("table"),
    Column("column"),
    ;

    /** Something a person calls: a function, method, constructor or stored procedure. */
    val isCallable: Boolean get() = this == Function || this == Method || this == Constructor || this == Procedure

    /** Something that names a type, and so holds members. */
    val isType: Boolean get() = this == Class || this == Interface || this == Object || this == Enum || this == Type || this == Table

    /** When several patterns match one name, the kind that says more wins: a callable or a type over a plain value. */
    val rank: Int get() = if (isCallable || isType) 2 else 1

    companion object {
        private val byTag = entries.associateBy { it.tag }

        /** The kind tagged [tag], or null for a tag Arc does not know. */
        fun of(tag: String): SymbolKind? = byTag[tag]
    }
}
