package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_already_in
import dev.chieppa.arc.design.generated.switcher_as_branch
import dev.chieppa.arc.design.generated.switcher_as_commit
import dev.chieppa.arc.design.generated.switcher_as_pull
import dev.chieppa.arc.design.generated.switcher_as_range
import dev.chieppa.arc.design.generated.switcher_whole_pull
import dev.chieppa.arc.ui.pulls.CheckMark
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource

/**
 * One way to open a pasted link: as its own source or as the whole pull request it sits in, marked when it is [held]
 * in the strip already. [facts] gives the titles.
 */
@Composable
fun OpenAsRow(option: OpenAs, facts: LinkFacts, held: Boolean, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val source = option.source
    PaletteRow(highlighted, onClick) {
        when (option) {
            is OpenAs.AsSource -> {
                Icon(sourceIcon(source), c.inkControl)
                RowTitle(asSourceText(source, facts), "")
                RowRef(SourceLabels.of(source))
            }
            is OpenAs.WholePull -> {
                Icon(sourceIcon(source), c.inkControl)
                RowTitle(stringResource(Res.string.switcher_whole_pull, facts.pull?.title ?: SourceLabels.of(source)), "")
                RowRef(SourceLabels.of(source))
            }
        }
        Spacer(Modifier.weight(1f))
        if (option is OpenAs.WholePull) facts.pull?.checks?.state?.let { CheckMark(it) }
        if (held) RowMeta(stringResource(Res.string.switcher_already_in))
    }
}

@Composable
private fun asSourceText(source: Source, facts: LinkFacts): String = when (source) {
    is Source.Commit -> stringResource(Res.string.switcher_as_commit, source.sha.value.take(ObjectId.SHORT_LENGTH))
    is Source.CommitRange -> stringResource(Res.string.switcher_as_range, SourceLabels.of(source).substringAfter('@'))
    is Source.BranchAgainstBase -> stringResource(Res.string.switcher_as_branch, source.branch.value)
    is Source.PullRequest -> stringResource(Res.string.switcher_as_pull, facts.title ?: SourceLabels.of(source))
}

