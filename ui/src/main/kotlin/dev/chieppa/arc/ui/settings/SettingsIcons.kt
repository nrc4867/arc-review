package dev.chieppa.arc.ui.settings

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.stretch.theme.StretchIcons

/** The icon names settings sections, cards and entries take, from Arc's and Stretch's icons, and the icons they name. */
object SettingsIcons {
    const val AT = "at"
    const val BELL = "bell"
    const val CHANGED_SINCE = "changed-since"
    const val CHECK_FAIL = "check-fail"
    const val CHECK_RUN = "check-run"
    const val CHECK_WAITING = "check-waiting"
    const val COMMENT = "comment"
    const val EYE = "eye"
    const val FILTER = "filter"
    const val FIT = "fit"
    const val GATE = "gate"
    const val LABEL = "label"
    const val PALETTE = "palette"
    const val PEOPLE = "people"
    const val PERSON = "person"
    const val PULL_REQUEST = "pull-request"
    const val RUNS = "runs"
    const val SETTINGS = "settings"
    const val SPLIT_VIEW = "split-view"
    const val THEME = "theme"
    const val WRAP = "wrap"

    private val icons: Map<String, ImageVector> by lazy {
        mapOf(
            AT to ArcIcons.At,
            BELL to ArcIcons.Bell,
            CHANGED_SINCE to ArcIcons.ChangedSince,
            CHECK_FAIL to ArcIcons.CheckFail,
            CHECK_RUN to ArcIcons.CheckRun,
            CHECK_WAITING to ArcIcons.CheckWaiting,
            COMMENT to ArcIcons.Comment,
            EYE to StretchIcons.Eye,
            FILTER to ArcIcons.Filter,
            FIT to StretchIcons.Fit,
            GATE to ArcIcons.Gate,
            LABEL to ArcIcons.Label,
            PALETTE to ArcIcons.Palette,
            PEOPLE to ArcIcons.People,
            PERSON to ArcIcons.Person,
            PULL_REQUEST to ArcIcons.PullRequest,
            RUNS to ArcIcons.Runs,
            SETTINGS to StretchIcons.Settings,
            SPLIT_VIEW to StretchIcons.SplitView,
            THEME to StretchIcons.Theme,
            WRAP to ArcIcons.Wrap,
        )
    }

    /** The icon [name] names, or null for a name neither set has. */
    fun of(name: String?): ImageVector? = name?.let(icons::get)
}
