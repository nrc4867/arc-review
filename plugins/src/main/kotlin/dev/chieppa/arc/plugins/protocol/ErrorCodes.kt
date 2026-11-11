package dev.chieppa.arc.plugins.protocol

/** The codes an [RpcError] carries. */
object ErrorCodes {
    /** The other side has no such method. */
    const val UNKNOWN_METHOD = "unknown-method"

    /** The params were not what the method takes. */
    const val BAD_PARAMS = "bad-params"

    /** The plugin's manifest does not declare the capability the call needs. */
    const val DENIED = "denied"

    /** The method ran and failed. */
    const val FAILED = "failed"
}
