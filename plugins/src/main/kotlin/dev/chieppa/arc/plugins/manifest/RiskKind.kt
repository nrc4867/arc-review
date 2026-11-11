package dev.chieppa.arc.plugins.manifest

/** A combination of capabilities that lets a plugin do more than each part says. */
enum class RiskKind {
    /** It can read code, files or the person's notes and send them to a host. */
    ReadsAndSends,

    /** It can write files with what it fetched from a host. */
    FetchesIntoFiles,

    /** It can run any program as the person. */
    RunsPrograms,
}
