package dev.chieppa.arc.plugins.host

/** What went wrong with a plugin. */
enum class DiagnosticKind {
    /** Its `plugin.json` cannot be used. */
    Manifest,

    /** Its code does not transpile. */
    Transpile,

    /** Its code fails type checking. */
    Type,

    /** Node, esbuild or the plugin's process could not be started, or its bundle failed to load. */
    Load,

    /** Its process kept ending. */
    Crash,
}
