package dev.chieppa.arc.ui.runs

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.core.actions.WhyMine
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.stretch.theme.StretchIcons

/** The icons runs show: what started one, and why it is the person's. */
object RunIcons {
    fun trigger(event: String, tag: Boolean): ImageVector = when {
        tag -> ArcIcons.Tag
        event == "schedule" -> ArcIcons.Schedule
        event == "workflow_dispatch" || event == "repository_dispatch" -> ArcIcons.RunWorkflow
        event.startsWith("pull_request") -> ArcIcons.PullRequest
        else -> ArcIcons.Artifact
    }

    fun why(why: WhyMine): ImageVector = when (why) {
        WhyMine.WaitingOnMe -> ArcIcons.Gate
        WhyMine.StartedByMe -> ArcIcons.Person
        WhyMine.MyPull, WhyMine.AssignedPull -> ArcIcons.PullRequest
        WhyMine.Watched -> StretchIcons.Eye
    }
}
