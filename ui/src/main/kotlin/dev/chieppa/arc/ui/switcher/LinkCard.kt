package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_added
import dev.chieppa.arc.design.generated.switcher_commit_of
import dev.chieppa.arc.design.generated.switcher_commits
import dev.chieppa.arc.design.generated.switcher_files
import dev.chieppa.arc.design.generated.switcher_in_pull
import dev.chieppa.arc.design.generated.switcher_meta
import dev.chieppa.arc.design.generated.switcher_removed
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * What a pasted link points at: its ref and title, then who made it and when, its files and lines, and the pull request
 * it sits in with where among its commits. A link to a repo no clone here comes from shows its ref in the error ink.
 */
@Composable
fun LinkCard(target: PastedTarget, facts: LinkFacts, nowSeconds: Long, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    val source = target.source ?: target.named
    Column(
        modifier
            .padding(start = StretchSpacing.space8, end = StretchSpacing.space8, top = StretchSpacing.space8, bottom = StretchSpacing.space4)
            .fillMaxWidth()
            .clip(shape)
            .background(c.surfacePanel)
            .border(Stretch.stroke, c.linePanel, shape)
            .padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space10),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            Icon(sourceIcon(source), c.inkControl)
            val refInk = if (target.source == null) c.errorInk else c.inkStrong
            BasicText(SourceLabels.of(source), style = Stretch.type.value.merge(TextStyle(color = refInk, fontWeight = FontWeight.Medium)), maxLines = 1)
            facts.title?.let {
                BasicText(it, Modifier.weight(1f, fill = false), style = Stretch.type.body.merge(TextStyle(color = c.inkStrong)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        val parts = metaParts(target, facts, nowSeconds)
        if (parts.isNotEmpty()) {
            Row(
                Modifier.padding(start = ArcSizes.linkCardIndent),
                horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                parts.forEachIndexed { i, part ->
                    if (i > 0 && part.dotBefore) SeparatorDot()
                    if (part.color != null) {
                        BasicText(part.text, style = Stretch.type.valueSm.merge(TextStyle(color = part.color)), maxLines = 1)
                    } else {
                        RowMeta(part.text, color = c.inkMuted)
                    }
                }
            }
        }
    }
}

/** One piece of the card's second line: a line count in code type and its [color], and whether a dot parts it from the piece before. */
private class MetaPart(val text: String, val color: Color? = null, val dotBefore: Boolean = true)

@Composable
private fun metaParts(target: PastedTarget, facts: LinkFacts, nowSeconds: Long): List<MetaPart> {
    val c = Stretch.colors
    return buildList {
        facts.author?.let { add(MetaPart(it)) }
        facts.time?.let { add(MetaPart(agoText(it, nowSeconds))) }
        facts.files?.let { files ->
            add(MetaPart(pluralStringResource(Res.plurals.switcher_files, files, files)))
            if (facts.added > 0) add(MetaPart(stringResource(Res.string.switcher_added, facts.added), c.deltaUp, dotBefore = false))
            if (facts.removed > 0) add(MetaPart(stringResource(Res.string.switcher_removed, facts.removed), c.deltaDown, dotBefore = false))
        }
        val pull = target.pullRequest?.takeIf { target.withinPull }
        if (pull != null) {
            val inPull = stringResource(Res.string.switcher_in_pull, SourceLabels.of(pull))
            val position = facts.position
            val of = facts.of
            add(MetaPart(if (position != null && of != null) stringResource(Res.string.switcher_meta, inPull, stringResource(Res.string.switcher_commit_of, position, of)) else inPull))
        } else if (target.source !is Source.Commit) {
            facts.commits?.let { add(MetaPart(pluralStringResource(Res.plurals.switcher_commits, it, it))) }
        }
    }
}
