package dev.chieppa.arc.codeintel

import dev.chieppa.arc.codeintel.model.AnswerOrigin
import dev.chieppa.arc.codeintel.model.CodeLocation
import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.arc.codeintel.model.SymbolTarget

/** Small answers for tests. */
object Answers {
    val byName = AnswerOrigin("tree-sitter", Precision.ByName, "kotlin")
    val exact = AnswerOrigin("kotlin-lsp", Precision.Exact, "kotlin")

    fun document(path: String, language: String = "kotlin") = DocumentRef("api", "9c1e2fa", path, language)

    fun target(path: String, line: Int, confidence: Confidence = Confidence.Confident, origin: AnswerOrigin = byName) =
        SymbolTarget(CodeLocation(document(path), CodeRange.onLine(line, 4, 9)), "TokenPair", "class", "class TokenPair", confidence, origin, "api#412")
}
