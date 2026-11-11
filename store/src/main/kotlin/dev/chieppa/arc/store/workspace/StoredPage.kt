package dev.chieppa.arc.store.workspace

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A page, a pane grid tab that shows no file, as written, by kind. */
@Serializable
sealed interface StoredPage {
    val id: String

    /** A saved filter's pull requests, by the filter's id. */
    @Serializable
    @SerialName("filter")
    data class Filter(override val id: String, val filter: String) : StoredPage

    /** A source's revision matrix, by the source's ref. */
    @Serializable
    @SerialName("revisions")
    data class RevisionMatrix(override val id: String, val source: String, val pinned: Boolean = false) : StoredPage

    /** A pull request's conversation, by the source's ref. */
    @Serializable
    @SerialName("conversation")
    data class Conversation(override val id: String, val source: String, val pinned: Boolean = false) : StoredPage

    /** A run filter's runs, by the filter's id. */
    @Serializable
    @SerialName("runs")
    data class Runs(override val id: String, val filter: String) : StoredPage

    /** One workflow run, by its repo and id. */
    @Serializable
    @SerialName("run")
    data class Run(override val id: String, val repo: String, val run: Long) : StoredPage

    /** A plugin's pane, by the plugin, its pane kind, the props it was opened with as JSON and the source's ref. */
    @Serializable
    @SerialName("plugin")
    data class Plugin(override val id: String, val plugin: String, val pane: String, val props: String = "{}", val source: String? = null) : StoredPage

    /** The Settings page, by the section it shows. */
    @Serializable
    @SerialName("settings")
    data class Settings(override val id: String, val section: String? = null) : StoredPage
}
