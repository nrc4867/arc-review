package dev.chieppa.arc.ui.sources

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons

/** The icon for a source's kind: a pull request, a commit, a commit range or a branch. */
fun sourceIcon(source: Source): ImageVector = when (source) {
    is Source.PullRequest -> ArcIcons.PullRequest
    is Source.Commit -> ArcIcons.Commit
    is Source.CommitRange -> ArcIcons.Range
    is Source.BranchAgainstBase -> ArcIcons.Branch
}
