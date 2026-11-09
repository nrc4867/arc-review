package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import dev.chieppa.arc.core.actions.MineRule
import dev.chieppa.arc.core.actions.RunFilter
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_workflow
import dev.chieppa.arc.design.generated.runs_all_repos
import dev.chieppa.arc.design.generated.runs_query
import dev.chieppa.arc.design.generated.runs_repo_filter
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.TextField
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The row under a run filter's tab: for Mine its four rules as switches with their counts, for any other filter its
 * query edited in place; then which repo to show, of [repos], and Run workflow.
 */
@Composable
fun RunsToolRow(filter: RunFilter, watch: RunWatch, repos: List<String>, repo: String?, onRepo: (String?) -> Unit, onRunWorkflow: () -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().height(RunSizes.toolRow).background(c.surfacePanel).padding(horizontal = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (filter.mine) {
            RunText(filter.name, RunTextKind.Eyebrow, c.inkMuted)
            for (rule in MineRule.entries) {
                MineRuleToggle(rule, rule in watch.watchlist.mineRules, watch.ruleCount(rule)) { watch.setRule(rule, it) }
            }
            Spacer(Modifier.weight(1f))
        } else {
            QueryField(filter, watch, Modifier.weight(1f))
        }
        val all = pluralStringResource(Res.plurals.runs_all_repos, repos.size, repos.size)
        Dropdown(
            listOf<String?>(null) + repos,
            repo,
            onRepo,
            { it ?: all },
            stringResource(Res.string.runs_repo_filter),
            Modifier.width(RunSizes.reposDropdown),
            icon = ArcIcons.Repo,
        )
        Button(stringResource(Res.string.run_workflow), onRunWorkflow, kind = ButtonKind.Primary, icon = ArcIcons.RunWorkflow)
    }
}

@Composable
private fun QueryField(filter: RunFilter, watch: RunWatch, modifier: Modifier) {
    var query by remember(filter.id, filter.query) { mutableStateOf(filter.query) }
    fun apply() {
        if (query.trim() != filter.query) watch.updateFilter(filter.copy(query = query.trim()))
    }
    TextField(
        query,
        { query = it },
        modifier.onFocusChanged { if (!it.isFocused) apply() }.onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown || event.key != Key.Enter) return@onPreviewKeyEvent false
            apply()
            true
        },
        icon = StretchIcons.Search,
        contentDescription = stringResource(Res.string.runs_query),
        mono = true,
    )
}
