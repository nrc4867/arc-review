package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** A key in the plugin's private store. */
@Serializable
data class StoreGetParams(val key: String)
