package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The tags of one [kind] in a doc comment; [tag] names an [HintSectionKind.Other] tag as written, without `@`. */
@Serializable
data class HintSection(val kind: HintSectionKind, val entries: List<HintEntry>, val tag: String? = null)
