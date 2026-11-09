package dev.chieppa.arc.core.actions.artifacts

/** How an artifact's file shows: as text, Markdown, JSON or a picture in a pane, a JUnit report as a table, HTML in the browser, or not at all. */
enum class ArtifactKind {
    Text,
    Markdown,
    Json,
    Image,
    JUnit,
    Html,
    Other,
    ;

    companion object {
        private val TEXT = setOf("txt", "log", "csv", "tsv", "yml", "yaml", "properties", "sql", "diff", "patch")
        private val IMAGES = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg")

        /** The kind of the file named [name], whose first bytes are [head]. */
        fun of(name: String, head: ByteArray = ByteArray(0)): ArtifactKind {
            val ext = name.substringAfterLast('.', "").lowercase()
            val start = head.decodeToString().trimStart()
            return when {
                ext == "md" || ext == "markdown" -> Markdown
                ext == "json" -> Json
                ext in IMAGES -> Image
                ext == "html" || ext == "htm" -> Html
                ext == "xml" && (start.contains("<testsuite") || start.contains("<testsuites")) -> JUnit
                ext == "xml" || ext in TEXT -> Text
                else -> Other
            }
        }
    }
}
