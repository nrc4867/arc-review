package dev.chieppa.arc.syntax.facts

/** How a name is used where it appears. */
enum class RefKind {
    /** Called, or constructed, with arguments. */
    Call,

    /** Written where a type goes. */
    Type,

    /** Any other use of the name. */
    Name,

    /** A SQL object named inside a string literal of another language. */
    SqlString,
}
