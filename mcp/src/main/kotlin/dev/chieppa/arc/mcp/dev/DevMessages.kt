package dev.chieppa.arc.mcp.dev

/** What the development tools tell the client's model when they refuse or wait, as text it reads. */
internal object DevMessages {
    const val UNKNOWN = "Arc knows no plugin with id"
    const val GIT = "comes from a git source; these tools reach only plugins from a folder"
    const val OFF = "is not enabled; only the person enables a plugin, in Arc under Settings › Plugins"
    const val WAITING = "is waiting for the person to approve what its manifest asks for in Arc under Settings › Plugins:"
    const val UNREADABLE = "has a plugin.json Arc cannot read:"
    const val NOT_RUNNING = "is not running; it is"
    const val READ_LOG = "plugin_log says why."
    const val NO_ACTION = "offers no action"
    const val NO_VIEW = "offers no panel or pane"
    const val NOT_SHOWN = "is not open in Arc's window; call again with open set to true"
    const val NO_SOURCE = "No source with that ref is open in Arc's window:"
    const val NOT_CAPTURED = "Arc's window could not draw that view now; it may be hidden behind another drawer or tab."
    const val UNKNOWN_EVENT = "Not an event plugins hear:"
    const val NOT_HEARD = "does not hear that event without the capability it needs"
    const val ACTION_DONE = "The action ran and returned."
    const val ACTION_WAITING = "The action is still running. It may be waiting on the person in Arc's window, such as a confirmation; plugin_log shows what it does next."
    const val ACTION_FAILED = "The action failed:"
    const val EVENT_SENT = "Sent"
    const val NO_ERRORS = "No type errors."
    const val NOT_SETTLED = "is still starting"
}
