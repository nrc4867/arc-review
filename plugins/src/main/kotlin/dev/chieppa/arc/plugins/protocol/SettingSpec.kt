package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * One setting a plugin declares: its [type], the [default] it has until set, the [label] Arc may show for it, the type
 * of a list's or map's values in [of], and a choice's [options].
 */
@Serializable
data class SettingSpec(
    val type: SettingType,
    val default: JsonElement? = null,
    val label: String? = null,
    val of: SettingType? = null,
    val options: List<String> = emptyList(),
)
