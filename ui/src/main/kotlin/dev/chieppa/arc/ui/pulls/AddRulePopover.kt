package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.rule_add
import dev.chieppa.arc.design.generated.rule_add_title
import dev.chieppa.arc.design.generated.rule_kind_filter
import dev.chieppa.arc.design.generated.rule_kind_label
import dev.chieppa.arc.design.generated.rule_label
import dev.chieppa.arc.design.generated.rule_repos
import dev.chieppa.arc.design.generated.rule_saved_filter
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.frame.Popover
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.TextField
import org.jetbrains.compose.resources.stringResource

/**
 * What the drawer's add-rule button opens: a rule for new pull requests in a saved filter, or for new pull requests
 * with a label in some repos. A field that does not read is edged in the error color.
 */
@Composable
fun AddRulePopover(open: Boolean, watch: PullWatch, onDismiss: () -> Unit) {
    val filters = watch.watchlist.filters
    var byLabel by remember { mutableStateOf(filters.isEmpty()) }
    var filterId by remember { mutableStateOf<String?>(null) }
    var label by remember { mutableStateOf("") }
    var repos by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }
    val chosen = filters.firstOrNull { it.id == filterId } ?: filters.firstOrNull()

    fun add() {
        val rule = if (byLabel) {
            val names = repos.split(',', ' ').filter { it.isNotBlank() }
            val parsed = names.mapNotNull(GitHubRepo::parse)
            if (label.isBlank() || parsed.size != names.size) return run { failed = true }
            NotifyRule.NewLabelled(watch.nextRuleId(), label.trim(), parsed.map { it.fullName })
        } else {
            NotifyRule.NewInFilter(watch.nextRuleId(), chosen?.id ?: return)
        }
        watch.putRule(rule)
        label = ""
        repos = ""
        onDismiss()
    }

    Popover(open, onDismiss, title = stringResource(Res.string.rule_add_title), width = ArcSizes.popoverWidth) {
        val row = Modifier.fillMaxWidth().padding(vertical = StretchSpacing.space4)
        val kinds = listOf(false, true)
        val kindLabels = mapOf(false to stringResource(Res.string.rule_kind_filter), true to stringResource(Res.string.rule_kind_label))
        Dropdown(kinds, byLabel, { byLabel = it; failed = false }, { kindLabels.getValue(it) }, stringResource(Res.string.rule_add_title), row)
        if (byLabel) {
            TextField(label, { label = it; failed = false }, row, placeholder = stringResource(Res.string.rule_label), icon = ArcIcons.Label, error = if (failed && label.isBlank()) "" else null)
            TextField(repos, { repos = it; failed = false }, row, placeholder = stringResource(Res.string.rule_repos), mono = true, error = if (failed) "" else null)
        } else if (chosen != null) {
            Dropdown(filters, chosen, { filterId = it.id }, { it.name }, stringResource(Res.string.rule_saved_filter), row, icon = ArcIcons.Filter)
        }
        Row(row, horizontalArrangement = Arrangement.End) {
            Button(stringResource(Res.string.rule_add), ::add, kind = ButtonKind.Primary)
        }
    }
}
