package dev.chieppa.arc.core.actions

/** The rules that together make a run the person's; each can be switched off. */
enum class MineRule {
    /** The person pushed, started or re-ran it. */
    StartedByMe,

    /** It ran for a pull request the person wrote or is assigned to. */
    OnMyPulls,

    /** A deployment of it waits for the person's approval, or it is a fork's run waiting for a maintainer. */
    WaitingOnMe,

    /** Its workflow, or the run itself, is watched. */
    Watched,
}
