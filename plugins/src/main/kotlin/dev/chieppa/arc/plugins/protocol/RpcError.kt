package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Why a request failed: one of [ErrorCodes] and a message for the plugin's log. */
@Serializable
data class RpcError(val code: String, val message: String)
