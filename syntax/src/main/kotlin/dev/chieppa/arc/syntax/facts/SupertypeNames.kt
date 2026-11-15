package dev.chieppa.arc.syntax.facts

/** Supertypes as a definition keeps them: the simple name a type is looked up by. */
internal object SupertypeNames {
    /**
     * The simple name in [written], a supertype as written after `extends`, `implements` or `:`: without type arguments,
     * a constructor call's arguments, a qualifier or nullability, such as `Store` for `com.example.Store<K, V>()`. Null
     * when nothing is left.
     */
    fun simple(written: String): String? {
        val bare = written.substringBefore('<').substringBefore('(').trim().removeSuffix("?")
        return bare.substringAfterLast('.').trim().takeIf { name -> name.isNotEmpty() && name.all { it.isLetterOrDigit() || it == '_' || it == '$' } }
    }
}
