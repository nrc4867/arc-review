package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** The memory a plugin's worker thread may use, as Node's `resourceLimits` take it; a worker over them is ended. */
@Serializable
data class WorkerLimits(
    val maxOldGenerationSizeMb: Int = OLD_GENERATION_MB,
    val maxYoungGenerationSizeMb: Int = YOUNG_GENERATION_MB,
    val stackSizeMb: Int = STACK_MB,
) {
    private companion object {
        const val OLD_GENERATION_MB = 256
        const val YOUNG_GENERATION_MB = 32
        const val STACK_MB = 4
    }
}
