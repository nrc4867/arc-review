package dev.chieppa.arc.core.actions

/** Which of the rules that are on find a run to be the person's, the strongest reason first. */
object MineRules {
    /** Every reason [run] is the person's under the rules [on], strongest first; empty when none. */
    fun reasons(run: WorkflowRun, facts: MineFacts, on: Set<MineRule>): List<WhyMine> = buildList {
        if (MineRule.WaitingOnMe in on && run.ref in facts.waitingOnMe) add(WhyMine.WaitingOnMe)
        if (MineRule.StartedByMe in on && facts.login != null && (same(run.actor, facts.login) || same(run.triggeringActor, facts.login))) add(WhyMine.StartedByMe)
        if (MineRule.OnMyPulls in on) {
            val pull = facts.pulls.firstOrNull { it.repo.equals(run.repo, true) && (it.number in run.pulls || (it.headSha != null && it.headSha == run.headSha)) }
            if (pull != null) add(if (pull.authored) WhyMine.MyPull else WhyMine.AssignedPull)
        }
        if (MineRule.Watched in on && watched(run, facts)) add(WhyMine.Watched)
    }

    /** The strongest reason [run] is the person's, or null when it is not. */
    fun why(run: WorkflowRun, facts: MineFacts, on: Set<MineRule>): WhyMine? = reasons(run, facts, on).firstOrNull()

    /** Whether the person watches [run] or its workflow. */
    fun watched(run: WorkflowRun, facts: MineFacts): Boolean =
        run.ref in facts.runs || facts.workflows.any { it.repo.equals(run.repo, true) && it.path == run.workflowPath }

    private fun same(a: String?, b: String): Boolean = a != null && a.equals(b, ignoreCase = true)
}
