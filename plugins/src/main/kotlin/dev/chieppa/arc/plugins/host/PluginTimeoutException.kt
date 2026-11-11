package dev.chieppa.arc.plugins.host

/** A plugin did not answer [method] in time. */
class PluginTimeoutException(val method: String) : Exception("No answer to $method")
