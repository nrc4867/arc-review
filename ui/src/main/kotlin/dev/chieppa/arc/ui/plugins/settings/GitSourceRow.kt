package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_accept_update
import dev.chieppa.arc.design.generated.plugins_remove_source
import dev.chieppa.arc.design.generated.plugins_review_update
import dev.chieppa.arc.design.generated.plugins_source_checked
import dev.chieppa.arc.design.generated.plugins_source_checked_today
import dev.chieppa.arc.design.generated.plugins_source_cloning
import dev.chieppa.arc.design.generated.plugins_source_count
import dev.chieppa.arc.design.generated.plugins_source_git
import dev.chieppa.arc.design.generated.plugins_source_pinned
import dev.chieppa.arc.design.generated.plugins_source_unreadable
import dev.chieppa.arc.design.generated.plugins_update_asks
import dev.chieppa.arc.design.generated.plugins_update_ready
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.gitsource.GitSourceState
import dev.chieppa.arc.plugins.gitsource.GitUpdate
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Label
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A git source's row, as board 9 draws it: its repo as a place, then its ref, the commit it is pinned to, when it was
 * last checked and how many plugins it holds. An update waiting shows under them as a chip, a warning one naming what
 * it asks for beyond what was approved, with a button to review its commits in Arc and one to accept it. A source that could not
 * be cloned or checked has its line highlighted.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GitSourceRow(
    source: PluginSourceSpec.Git,
    state: GitSourceState?,
    plugins: Int,
    now: Long,
    onReview: (GitUpdate) -> Unit,
    onAccept: () -> Unit,
    onRemove: () -> Unit,
) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = PluginSettingsSizes.sourceRow).padding(horizontal = StretchSpacing.space14).testTag(PluginSettingsTags.GIT_SOURCE + source.url),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(ArcIcons.Repo, c.inkControl)
        Column(Modifier.weight(1f).padding(vertical = StretchSpacing.space8), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            Label(GitSourceLabels.place(source.url), Stretch.type.valueSm.copy(fontWeight = FontWeight.SemiBold), c.inkStrong)
            val failed = state?.failed == true
            Label(lineOf(source, state, plugins, now), Stretch.type.bodySm, if (failed) c.errorInk else c.inkSubtle)
            state?.update?.let { update ->
                FlowRow(
                    Modifier.padding(top = StretchSpacing.space6),
                    horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
                    verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    UpdateChip(update)
                    Button(stringResource(Res.string.plugins_review_update), { onReview(update) }, icon = StretchIcons.Eye)
                    Button(stringResource(Res.string.plugins_accept_update), onAccept, kind = ButtonKind.Quiet, icon = StretchIcons.Check, enabled = state.busy.not())
                }
            }
        }
        IconButton(StretchIcons.Close, stringResource(Res.string.plugins_remove_source), onRemove)
    }
}

@Composable
private fun UpdateChip(update: GitUpdate) {
    val version = update.version ?: GitSourceLabels.short(update.sha)
    if (update.asks.isEmpty()) {
        Chip(stringResource(Res.string.plugins_update_ready, version))
        return
    }
    val asks = update.asks.map { capabilityLabel(it) }.joinToString(GitSourceLabels.SEPARATOR)
    val text = stringResource(Res.string.plugins_update_asks, version, asks)
    Chip(text, kind = ChipKind.Warn, mono = true, icon = ArcIcons.Warning, description = text)
}

@Composable
private fun lineOf(source: PluginSourceSpec.Git, state: GitSourceState?, plugins: Int, now: Long): String {
    val parts = buildList {
        add(stringResource(Res.string.plugins_source_git, source.ref))
        source.pinned?.let { add(stringResource(Res.string.plugins_source_pinned, GitSourceLabels.short(it))) }
        when {
            state?.failed == true -> add(stringResource(Res.string.plugins_source_unreadable))
            source.pinned == null -> add(stringResource(Res.string.plugins_source_cloning))
        }
        state?.checkedAt?.let { at ->
            add(
                if (GitSourceLabels.today(at, now)) stringResource(Res.string.plugins_source_checked_today, GitSourceLabels.time(at))
                else stringResource(Res.string.plugins_source_checked, GitSourceLabels.day(at)),
            )
        }
    }
    return pluralStringResource(Res.plurals.plugins_source_count, plugins, parts.joinToString(GitSourceLabels.SEPARATOR), plugins)
}
