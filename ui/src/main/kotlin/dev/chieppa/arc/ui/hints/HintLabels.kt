package dev.chieppa.arc.ui.hints

import androidx.compose.runtime.Composable
import dev.chieppa.arc.codeintel.model.HintSection
import dev.chieppa.arc.codeintel.model.HintSectionKind
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.hint_authors
import dev.chieppa.arc.design.generated.hint_examples
import dev.chieppa.arc.design.generated.hint_format_javadoc
import dev.chieppa.arc.design.generated.hint_format_jsdoc
import dev.chieppa.arc.design.generated.hint_format_kdoc
import dev.chieppa.arc.design.generated.hint_format_sql
import dev.chieppa.arc.design.generated.hint_parameters
import dev.chieppa.arc.design.generated.hint_properties
import dev.chieppa.arc.design.generated.hint_returns
import dev.chieppa.arc.design.generated.hint_see
import dev.chieppa.arc.design.generated.hint_since
import dev.chieppa.arc.design.generated.hint_throws
import dev.chieppa.arc.design.generated.hint_type_parameters
import dev.chieppa.arc.index.hint.HintFormats
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.stretch.frame.caps
import org.jetbrains.compose.resources.stringResource

/** The words a symbol card shows for what a doc comment holds. */
object HintLabels {
    /** A section's label: its kind's name, or an other tag's own name. */
    @Composable
    fun of(section: HintSection): String = when (section.kind) {
        HintSectionKind.Parameters -> stringResource(Res.string.hint_parameters)
        HintSectionKind.Properties -> stringResource(Res.string.hint_properties)
        HintSectionKind.TypeParameters -> stringResource(Res.string.hint_type_parameters)
        HintSectionKind.Returns -> stringResource(Res.string.hint_returns)
        HintSectionKind.Throws -> stringResource(Res.string.hint_throws)
        HintSectionKind.See -> stringResource(Res.string.hint_see)
        HintSectionKind.Since -> stringResource(Res.string.hint_since)
        HintSectionKind.Examples -> stringResource(Res.string.hint_examples)
        HintSectionKind.Authors -> stringResource(Res.string.hint_authors)
        HintSectionKind.Other -> section.tag.orEmpty()
    }

    /** The name of the convention a doc comment is written in, or the id as given for one Arc does not know. */
    @Composable
    fun format(format: String): String = when (format) {
        HintFormats.JAVADOC -> stringResource(Res.string.hint_format_javadoc)
        HintFormats.KDOC -> stringResource(Res.string.hint_format_kdoc)
        HintFormats.JSDOC -> stringResource(Res.string.hint_format_jsdoc)
        HintFormats.SQL -> stringResource(Res.string.hint_format_sql)
        else -> format
    }

    /** The key cap of the modifier a click through holds: Ctrl, or Cmd on macOS. */
    fun clickModifier(): List<String> = ArcKeys.SUBMIT_COMMENT.caps().take(1)
}
