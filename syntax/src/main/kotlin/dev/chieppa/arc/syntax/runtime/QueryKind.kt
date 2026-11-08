package dev.chieppa.arc.syntax.runtime

/**
 * The query files Arc keeps for each grammar under `queries/<grammar>/<file>`. [Highlights] colours code; the others
 * feed the index: [Definitions] (`@definition.<kind>` with `@name`, a callable's `@params` list, an optional `@type`,
 * and `@param` captures that belong to the innermost `@params` around them), [References] (`@reference.call` with
 * `@name` and its `@args` list, `@arg` captures that belong to the innermost `@args`, `@reference.type`, and a SQL
 * name's `@schema`), [Names] (every identifier as `@name`) and [Structure] (`@package`, `@import` and `@string`).
 */
enum class QueryKind(val file: String) {
    Highlights("highlights.scm"),
    Definitions("definitions.scm"),
    References("references.scm"),
    Names("names.scm"),
    Structure("structure.scm"),
}
