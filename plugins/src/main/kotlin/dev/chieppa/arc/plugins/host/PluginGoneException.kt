package dev.chieppa.arc.plugins.host

/** A request was waiting when the plugin's process ended. */
class PluginGoneException : Exception("The plugin's process ended")
