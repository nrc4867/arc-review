package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.gitsource.GitUpdate
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.launch

/** Reviewing a git source's waiting update in Arc itself: its commits from the pin, as a range in the source's clone. */
object GitUpdateReview {
    /**
     * Follows [source]'s clone as a repo and opens the commits after its pin up to [update]'s, the range a person
     * reviews before accepting it.
     */
    fun open(workbench: Workbench, source: PluginSourceSpec.Git, update: GitUpdate) {
        val manager = workbench.plugins.manager ?: return
        val pinned = source.pinned ?: return
        val clone = manager.gitClone(source.url) ?: return
        workbench.scope.launch {
            val repo = workbench.sources.addRepo(clone) ?: return@launch
            workbench.openSource(Source.CommitRange(repo, CommitSha(pinned), CommitSha(update.sha)))
        }
    }
}
