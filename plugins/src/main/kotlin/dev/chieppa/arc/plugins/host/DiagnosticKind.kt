package dev.chieppa.arc.plugins.host

/** What went wrong with a plugin. */
enum class DiagnosticKind {
    /** Its `plugin.json` cannot be used. */
    Manifest,

    /** Its code does not convert from TypeScript. */
    Transpile,

    /** Its code fails type checking. */
    Type,

    /** Node or the plugin's worker could not be started, or its entry failed to load. */
    Load,

    /** Its worker kept ending. */
    Crash,
}
