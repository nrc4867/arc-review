package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.PendingDeployment
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.deploy_approve
import dev.chieppa.arc.design.generated.deploy_comment
import dev.chieppa.arc.design.generated.deploy_open_commits
import dev.chieppa.arc.design.generated.deploy_reject
import dev.chieppa.arc.design.generated.deploy_reviewers
import dev.chieppa.arc.design.generated.deploy_sent_as_you
import dev.chieppa.arc.design.generated.deploy_ships
import dev.chieppa.arc.design.generated.deploy_title
import dev.chieppa.arc.design.generated.deploy_waiting
import dev.chieppa.arc.host.github.actions.ShipsSince
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.arc.ui.pulls.shortAge
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openRunRange
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Checkbox
import dev.chieppa.stretch.ui.TextArea
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Reviewing a run held at environments the person may approve: each environment to tick with who may approve it,
 * the pull requests that ship since the environment's last deployment with a button to open those commits as a
 * source, a comment, and Reject or Approve and deploy, which go to GitHub as the person. A refused review stays with
 * the buttons in the error color.
 */
@Composable
fun DeploymentCard(run: WorkflowRun, pending: List<PendingDeployment>, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val now = currentTime()
    val scope = rememberCoroutineScope()
    var picked by remember(run.ref) { mutableStateOf(pending.map { it.environmentId }.toSet()) }
    var comment by remember(run.ref) { mutableStateOf("") }
    var failed by remember(run.ref) { mutableStateOf(false) }
    var ships by remember(run.ref) { mutableStateOf<ShipsSince?>(null) }
    LaunchedEffect(run.ref) {
        val repo = GitHubRepo.parse(run.repo) ?: return@LaunchedEffect
        ships = try {
            workbench.actions.shipsSince(repo, pending.first().environment, run.headSha)
        } catch (_: GitHubException) {
            null
        }
    }
    val shape = Stretch.shapes.cardShape
    Column(modifier.verticalScroll(rememberScrollState()).padding(StretchSpacing.space12)) {
        Column(Modifier.fillMaxWidth().background(c.surfacePanel, shape).border(Stretch.stroke, c.linePanel, shape).padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space10)) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                Icon(ArcIcons.Gate, c.warnInk)
                RunText(stringResource(Res.string.deploy_title), RunTextKind.Strong, c.inkStrong, Modifier.weight(1f))
                pending.mapNotNull { it.waitingSince }.minOrNull()?.let { RunText(stringResource(Res.string.deploy_waiting, shortAge(it.toEpochMilli(), now)), RunTextKind.Small, c.warnInk) }
            }
            for (environment in pending) {
                Column {
                    Checkbox(environment.environmentId in picked, { on -> picked = if (on) picked + environment.environmentId else picked - environment.environmentId }, environment.environment)
                    RunText(stringResource(Res.string.deploy_reviewers, environment.reviewers.joinToString(", ")), RunTextKind.Small, c.inkSubtle, Modifier.padding(start = ArcSizes.pullIcon + StretchSpacing.space8), maxLines = 2)
                }
            }
            ships?.let { shipped ->
                Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4)) {
                    RunText(stringResource(Res.string.deploy_ships, pending.first().environment), RunTextKind.Eyebrow, c.inkMuted)
                    for (change in shipped.changes.filter { it.pull != null }.take(MAX_SHIPPED)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                            Icon(ArcIcons.PullRequest, c.inkMuted, size = ArcSizes.smallIcon)
                            RunText("${run.repo.substringAfter('/')}#${change.pull}", RunTextKind.MonoSmall, c.inkControl)
                            RunText(change.title, RunTextKind.Small, c.inkMuted, Modifier.weight(1f))
                        }
                    }
                    Button(
                        pluralStringResource(Res.plurals.deploy_open_commits, shipped.total, shipped.total),
                        { workbench.openRunRange(run, shipped.base, shipped.head) },
                        kind = ButtonKind.Quiet,
                        icon = dev.chieppa.stretch.theme.StretchIcons.Layers,
                    )
                }
            }
            TextArea(comment, { comment = it }, placeholder = stringResource(Res.string.deploy_comment), minLines = 2, maxLines = 6)
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                RunText(stringResource(Res.string.deploy_sent_as_you), RunTextKind.Small, if (failed) c.errorInk else c.inkSubtle, Modifier.weight(1f))
                fun review(approve: Boolean) {
                    scope.launch { failed = !workbench.runs.reviewDeployments(run, picked.toList(), approve, comment) }
                }
                Button(stringResource(Res.string.deploy_reject), { review(false) }, kind = ButtonKind.Quiet, enabled = picked.isNotEmpty())
                Button(stringResource(Res.string.deploy_approve), { review(true) }, kind = ButtonKind.Primary, icon = ArcIcons.Gate, enabled = picked.isNotEmpty())
            }
        }
    }
}

private const val MAX_SHIPPED = 8
