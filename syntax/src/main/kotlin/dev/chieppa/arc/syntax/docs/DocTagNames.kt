package dev.chieppa.arc.syntax.docs

/** The names of block tags the parsers recognise, without the `@`. */
internal object DocTagNames {
    const val PARAM = "param"
    const val ARG = "arg"
    const val ARGUMENT = "argument"
    const val PROPERTY = "property"
    const val RETURN = "return"
    const val RETURNS = "returns"
    const val THROWS = "throws"
    const val EXCEPTION = "exception"
    const val SEE = "see"
    const val SINCE = "since"
    const val AUTHOR = "author"
    const val DEPRECATED = "deprecated"
    const val SAMPLE = "sample"
    const val EXAMPLE = "example"
    const val REMARKS = "remarks"
    const val TEMPLATE = "template"
    const val TYPE_PARAM = "typeParam"
    const val DEFAULT = "default"
    const val DEFAULT_VALUE = "defaultValue"

    /** Tags that end a JSDoc example body; any other `@word` line in it is example code. */
    val javaScriptKnown = setOf(
        PARAM, ARG, ARGUMENT, RETURN, RETURNS, THROWS, EXCEPTION, SEE, SINCE, AUTHOR, DEPRECATED, EXAMPLE, REMARKS,
        TEMPLATE, TYPE_PARAM, DEFAULT, DEFAULT_VALUE,
    )
}
