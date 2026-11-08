package dev.chieppa.arc.ui.start

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.stretch.theme.StretchIcons

/** The icon names start cards, rows and buttons take, from Arc's and Stretch's icons, and the icons they name. */
object StartIcons {
    const val BRANCH = "branch"
    const val CHANGED_SINCE = "changed-since"
    const val CHECK_FAIL = "check-fail"
    const val CHECK_RUN = "check-run"
    const val CLOSE = "close"
    const val COMMENT = "comment"
    const val COMMIT = "commit"
    const val CONTINUE = "continue"
    const val EXTERNAL_LINK = "external-link"
    const val EYE = "eye"
    const val FILE = "file"
    const val FILTER = "filter"
    const val GUIDE = "guide"
    const val KEYBOARD = "keyboard"
    const val MATRIX = "matrix"
    const val NOTE = "note"
    const val PROBLEM = "problem"
    const val PULL_REQUEST = "pull-request"
    const val RANGE = "range"
    const val REPLY = "reply"
    const val REPO = "repo"
    const val ROTATE = "rotate"
    const val SETTINGS = "settings"
    const val SPLIT_VIEW = "split-view"
    const val STAGE = "stage"

    private val icons: Map<String, ImageVector> by lazy {
        mapOf(
            BRANCH to ArcIcons.Branch,
            CHANGED_SINCE to ArcIcons.ChangedSince,
            CHECK_FAIL to ArcIcons.CheckFail,
            CHECK_RUN to ArcIcons.CheckRun,
            CLOSE to StretchIcons.Close,
            COMMENT to ArcIcons.Comment,
            COMMIT to ArcIcons.Commit,
            CONTINUE to ArcIcons.Continue,
            EXTERNAL_LINK to StretchIcons.ExternalLink,
            EYE to StretchIcons.Eye,
            FILE to StretchIcons.File,
            FILTER to ArcIcons.Filter,
            GUIDE to ArcIcons.Guide,
            KEYBOARD to ArcIcons.Keyboard,
            MATRIX to ArcIcons.Matrix,
            NOTE to ArcIcons.Note,
            PROBLEM to ArcIcons.Problem,
            PULL_REQUEST to ArcIcons.PullRequest,
            RANGE to ArcIcons.Range,
            REPLY to ArcIcons.Reply,
            REPO to ArcIcons.Repo,
            ROTATE to StretchIcons.Rotate,
            SETTINGS to StretchIcons.Settings,
            SPLIT_VIEW to StretchIcons.SplitView,
            STAGE to ArcIcons.Stage,
        )
    }

    /** The icon [name] names, or null for a name neither set has. */
    fun of(name: String?): ImageVector? = name?.let(icons::get)

    /** The icon name for [source]'s kind. */
    fun of(source: Source): String = when (source) {
        is Source.PullRequest -> PULL_REQUEST
        is Source.Commit -> COMMIT
        is Source.CommitRange -> RANGE
        is Source.BranchAgainstBase -> BRANCH
    }
}
