package dev.chieppa.arc.core.actions

/** Why a run is the person's, the reason shown on its row, by the rule that found it. */
enum class WhyMine(val rule: MineRule) {
    WaitingOnMe(MineRule.WaitingOnMe),
    StartedByMe(MineRule.StartedByMe),
    MyPull(MineRule.OnMyPulls),
    AssignedPull(MineRule.OnMyPulls),
    Watched(MineRule.Watched),
}
