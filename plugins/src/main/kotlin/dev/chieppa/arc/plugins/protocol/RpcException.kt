package dev.chieppa.arc.plugins.protocol

/** A request that came back with [error], or one a handler refuses with it. */
class RpcException(val error: RpcError) : Exception("${error.code}: ${error.message}")
