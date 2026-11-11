package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** What a plugin contributes, as its `definePlugin` declares it. */
@Serializable
data class InitializeResult(val contributions: Contributions = Contributions())
