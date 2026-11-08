package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * One file at one revision: the [repo] by Arc's name for it, the [revision] (a commit sha) and the [path] in the repo.
 * [language] is Arc's language id for it (`kotlin`, `java`, `javascript`, `typescript`, `tsx`, `tsql`) when known.
 */
@Serializable
data class DocumentRef(val repo: String, val revision: String, val path: String, val language: String? = null)
