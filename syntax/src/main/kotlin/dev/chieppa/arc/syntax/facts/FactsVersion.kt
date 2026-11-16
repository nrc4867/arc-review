package dev.chieppa.arc.syntax.facts

/**
 * Which build of fact extraction this is: a hash of the `syntax` module's code and queries and the tree-sitter pins,
 * written by `:syntax:writeFactsVersion`. Facts kept from another version are not read.
 */
object FactsVersion {
    private const val RESOURCE = "/dev/chieppa/arc/syntax/facts/facts-version.txt"

    val current: String by lazy {
        FactsVersion::class.java.getResource(RESOURCE)?.readText()?.trim() ?: error("No $RESOURCE bundled")
    }
}
