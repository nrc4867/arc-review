package dev.chieppa.arc.syntax.highlight

/**
 * Which [TokenStyle] a highlight capture name draws as. Names match by their first dotted part, so
 * `function.method.call` is a function; grammars use both the current names (`keyword.conditional`) and the older
 * ones (`conditional`, `repeat`, `include`), and both are listed. Plain captures such as `variable` or `punctuation`
 * clear an outer colour, as a variable inside a template string; captures Arc does not know are ignored.
 */
internal object CaptureStyles {
    private val byHead = mapOf(
        "keyword" to TokenStyle.Keyword,
        "conditional" to TokenStyle.Keyword,
        "repeat" to TokenStyle.Keyword,
        "include" to TokenStyle.Keyword,
        "exception" to TokenStyle.Keyword,
        "storageclass" to TokenStyle.Keyword,
        "tag" to TokenStyle.Keyword,
        "string" to TokenStyle.String,
        "character" to TokenStyle.String,
        "escape" to TokenStyle.String,
        "number" to TokenStyle.Number,
        "float" to TokenStyle.Number,
        "boolean" to TokenStyle.Number,
        "comment" to TokenStyle.Comment,
        "type" to TokenStyle.Type,
        "constructor" to TokenStyle.Type,
        "attribute" to TokenStyle.Type,
        "function" to TokenStyle.Function,
        "method" to TokenStyle.Function,
    )

    private val exact = mapOf(
        "constant.builtin" to TokenStyle.Number,
        "type.qualifier" to TokenStyle.Keyword,
        "keyword.operator" to TokenStyle.Keyword,
        "string.special.key" to TokenStyle.Function,
    )

    private val plainHeads = setOf("variable", "property", "parameter", "punctuation", "operator", "field", "constant", "label", "namespace", "none", "embedded")

    sealed interface Resolved {
        data class Styled(val style: TokenStyle) : Resolved
        data object Plain : Resolved
        data object Ignored : Resolved
    }

    fun of(capture: String): Resolved {
        exact[capture]?.let { return Resolved.Styled(it) }
        val head = capture.substringBefore('.')
        byHead[head]?.let { return Resolved.Styled(it) }
        return if (head in plainHeads) Resolved.Plain else Resolved.Ignored
    }
}
