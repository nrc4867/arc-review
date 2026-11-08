package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.core.start.StartCard
import dev.chieppa.arc.core.start.StartColumn
import dev.chieppa.arc.core.start.StartContribution
import dev.chieppa.arc.core.start.StartLink
import dev.chieppa.arc.core.start.StartMenuItem
import dev.chieppa.arc.core.start.StartRow
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.start.StartTrailing
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_add
import dev.chieppa.arc.design.generated.start_all_settings
import dev.chieppa.arc.design.generated.start_clear
import dev.chieppa.arc.design.generated.start_guide
import dev.chieppa.arc.design.generated.start_help
import dev.chieppa.arc.design.generated.start_keys
import dev.chieppa.arc.design.generated.start_meta
import dev.chieppa.arc.design.generated.start_recent
import dev.chieppa.arc.design.generated.start_report
import dev.chieppa.arc.design.generated.start_repositories
import dev.chieppa.arc.design.generated.start_settings
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.LocalActionText
import org.jetbrains.compose.resources.stringResource

private val SCREEN = setOf(StartScreenKind.NothingOpen)

private val KEYS = listOf(
    ArcActionIds.GO_TO_ANYTHING,
    ArcActionIds.COMMANDS,
    ArcActionIds.OPEN_FILE,
    ArcActionIds.NEXT_FILE,
    ArcActionIds.NEXT_CHANGE,
    ArcActionIds.TOGGLE_REVIEWED,
    ArcActionIds.PUSH_REVIEW,
)

/** Arc's cards with nothing open: Recent and Repositories, then Keys, Settings and Help beside them. */
@Composable
fun nothingOpenCards(workbench: Workbench): StartContribution {
    return StartContribution(
        cards = listOf(
            StartCard(
                StartCards.RECENT, stringResource(Res.string.start_recent), SCREEN, StartColumn.Main, StartIcons.ROTATE,
                rows = recentRows(workbench),
                header = StartLink(stringResource(Res.string.start_clear), ArcStartActions.of(ArcActionIds.CLEAR_RECENT)),
            ),
            StartCard(
                StartCards.REPOSITORIES, stringResource(Res.string.start_repositories), SCREEN, StartColumn.Main, StartIcons.REPO,
                rows = repoRows(workbench),
                header = StartLink(stringResource(Res.string.start_add), ArcStartActions.of(ArcActionIds.ADD_SOURCE)),
            ),
            StartCard(StartCards.KEYS, stringResource(Res.string.start_keys), SCREEN, StartColumn.Side, StartIcons.KEYBOARD, rows = keyRows(workbench, KEYS)),
            StartCard(
                StartCards.SETTINGS, stringResource(Res.string.start_settings), SCREEN, StartColumn.Side, StartIcons.SETTINGS,
                links = listOf(StartLink(stringResource(Res.string.start_all_settings), ArcStartActions.of(ArcActionIds.SETTINGS))),
            ),
            StartCard(
                StartCards.HELP, stringResource(Res.string.start_help), SCREEN, StartColumn.Side, StartIcons.GUIDE,
                rows = listOf(
                    StartRow(stringResource(Res.string.start_guide), StartIcons.GUIDE, action = ArcStartActions.openLink(ArcLinks.GUIDE)),
                    StartRow(stringResource(Res.string.start_report), StartIcons.PROBLEM, action = ArcStartActions.openLink(ArcLinks.NEW_ISSUE)),
                ),
            ),
        ),
    )
}

/** The recently opened sources not in the strip, each opening it again. */
@Composable
private fun recentRows(workbench: Workbench): List<StartRow> {
    val forget = LocalActionText.current(ArcActionIds.FORGET_RECENT.value)
    val open = workbench.state.workspace.sources.map { it.ref }.toSet()
    return workbench.start.recent.sources.filter { it.ref !in open }.mapNotNull { recent ->
        val source = SourceRefs.parse(recent.ref) ?: return@mapNotNull null
        val label = SourceLabels.of(source)
        val home = workbench.gitHubRepos[source.repo]?.fullName
        StartRow(
            title = recent.title ?: label,
            icon = StartIcons.of(source),
            subtitle = if (recent.title == null) home else home?.let { stringResource(Res.string.start_meta, label, it) } ?: label,
            trailing = StartTrailing.Time(recent.at),
            action = ArcStartActions.openSource(recent.ref),
            menu = listOf(StartMenuItem(forget, ArcStartActions.forgetRecent(recent.ref))),
        )
    }
}

/** The repos Arc knows, each opening its GitHub page when it was cloned from GitHub. */
private fun repoRows(workbench: Workbench): List<StartRow> =
    workbench.state.repos.entries.sortedBy { it.key.value }.map { (name, folder) ->
        val home = workbench.gitHubRepos[name]?.fullName
        StartRow(
            title = name.value,
            icon = StartIcons.REPO,
            subtitle = home ?: folder.toString(),
            action = home?.let { ArcStartActions.openLink(ArcLinks.repo(it)) },
        )
    }
