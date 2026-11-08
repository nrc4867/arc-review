package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.AnswerOrigin
import dev.chieppa.arc.codeintel.model.CodeDecoration
import dev.chieppa.arc.codeintel.model.CodeLocation
import dev.chieppa.arc.codeintel.model.CodePosition
import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.DecorationStyle
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.stretch.code.CodeDecorationStyle
import dev.chieppa.stretch.code.CodeRange as LineRange
import kotlin.test.Test
import kotlin.test.assertEquals

class PaneDecorationsTest {
    private val document = DocumentRef("api", "a1", "src/A.kt", "kotlin")
    private val lengths = listOf(10, 4, 12)

    @Test
    fun marksOnThisFileAndProvidedDecorationsCutOnePerLine() {
        val focus = listOf(
            FocusMark(CodeLocation(document, CodeRange.onLine(1, 2, 3)), DecorationStyle.Definition),
            FocusMark(CodeLocation(document.copy(path = "src/B.kt"), CodeRange.onLine(1, 0, 3)), DecorationStyle.Reference),
        )
        val provided = listOf(CodeDecoration(CodeRange(CodePosition(1, 8), CodePosition(3, 5)), DecorationStyle.Warning, AnswerOrigin("plugin", Precision.Exact), "slow"))
        val decorations = PaneDecorations.of(document, focus, provided) { lengths.getOrNull(it - 1) }
        assertEquals(
            listOf(
                LineDecoration(LineRange(1, 2, 5), CodeDecorationStyle.Definition, null),
                LineDecoration(LineRange(1, 8, 10), CodeDecorationStyle.Warning, "slow"),
                LineDecoration(LineRange(2, 0, 4), CodeDecorationStyle.Warning, "slow"),
                LineDecoration(LineRange(3, 0, 5), CodeDecorationStyle.Warning, "slow"),
            ),
            decorations,
        )
    }
}
