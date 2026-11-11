package dev.chieppa.arc.syntax.docs

/** The convention a doc comment is written in. */
enum class DocFormat {
    JavaDoc,
    KDoc,

    /** JSDoc, and TSDoc, which shares its shape. */
    JsDoc,

    /** Plain `--` or block comments above a T-SQL definition, which have no tags. */
    SqlComment,
}
