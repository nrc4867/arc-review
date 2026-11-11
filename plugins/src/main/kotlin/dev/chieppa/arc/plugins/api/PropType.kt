package dev.chieppa.arc.plugins.api

/** The TypeScript type of a component prop or API member, written out as [ts]. */
sealed interface PropType {
    val ts: String

    data object Text : PropType {
        override val ts = "string"
    }

    data object Number : PropType {
        override val ts = "number"
    }

    data object Bool : PropType {
        override val ts = "boolean"
    }

    /** Anything React can render. */
    data object Node : PropType {
        override val ts = "ReactNode"
    }

    /** One of [values], as string literals. */
    data class OneOf(val values: List<String>) : PropType {
        override val ts: String get() = values.joinToString(" | ") { "\"$it\"" }
    }

    data class ListOf(val of: PropType) : PropType {
        override val ts: String get() = if (of is OneOf || of is Handler) "readonly (${of.ts})[]" else "readonly ${of.ts}[]"
    }

    /** A function the plugin passes, called with [params] when a person uses the component. */
    data class Handler(val params: List<Pair<String, PropType>> = emptyList(), val returns: String = "void") : PropType {
        override val ts: String get() = "(" + params.joinToString(", ") { (name, type) -> "$name: ${type.ts}" } + ") => $returns"
    }

    /** A type written out as it is, such as a generated interface's name or a generic parameter. */
    data class Named(override val ts: String) : PropType
}
