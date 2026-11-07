package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import dev.chieppa.arc.core.revision.RevisionMatrix
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_files
import dev.chieppa.arc.design.generated.matrix_only_changed
import dev.chieppa.arc.design.generated.matrix_threads_open
import dev.chieppa.arc.design.generated.matrix_with_threads
import dev.chieppa.arc.design.generated.revisions_revisions
import dev.chieppa.arc.design.generated.revisions_separator
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ToggleButton
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The row under the revision matrix's tab: how many files, revisions and open threads it shows, and the filters for
 * the files changed after their review and the files with threads.
 */
@Composable
fun MatrixToolRow(
    matrix: RevisionMatrix,
    onlyChanged: Boolean,
    onOnlyChanged: (Boolean) -> Unit,
    withThreads: Boolean,
    onWithThreads: (Boolean) -> Unit,
) {
    val c = Stretch.colors
    val files = pluralStringResource(Res.plurals.matrix_files, matrix.rows.size, matrix.rows.size)
    val separator = stringResource(Res.string.revisions_separator)
    val revisions = pluralStringResource(Res.plurals.revisions_revisions, matrix.revisions.size, matrix.revisions.size)
    val open = matrix.openThreads
    val threads = pluralStringResource(Res.plurals.matrix_threads_open, open, open)
    val summary = buildAnnotatedString {
        withStyle(SpanStyle(color = c.inkStrong, fontWeight = FontWeight.Medium)) { append(files) }
        append(separator + revisions + separator + threads)
    }
    Row(
        Modifier.fillMaxWidth().height(ArcSizes.matrixToolRow).background(c.surfacePanel)
            .padding(start = StretchSpacing.space10, end = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            summary,
            Modifier.weight(1f),
            style = Stretch.type.value.merge(TextStyle(color = c.inkMuted)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ToggleButton(stringResource(Res.string.matrix_only_changed), onlyChanged, onOnlyChanged, icon = ArcIcons.ChangedSince, height = ArcSizes.matrixToggle)
        ToggleButton(stringResource(Res.string.matrix_with_threads), withThreads, onWithThreads, icon = ArcIcons.Comment, height = ArcSizes.matrixToggle)
    }
}
