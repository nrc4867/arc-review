package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** What the tags of a [HintSection] say. */
@Serializable
enum class HintSectionKind { Parameters, Properties, TypeParameters, Returns, Throws, See, Since, Examples, Authors, Other }
