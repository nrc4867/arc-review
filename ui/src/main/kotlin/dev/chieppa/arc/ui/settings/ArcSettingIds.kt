package dev.chieppa.arc.ui.settings

import dev.chieppa.arc.core.actions.RunNotify

/** The ids and rail order of Arc's own settings sections, cards and entries. */
object ArcSettingIds {
    const val GENERAL = "arc.general"
    const val APPEARANCE = "arc.appearance"
    const val ACCOUNTS = "arc.accounts"
    const val NOTIFICATIONS = "arc.notifications"

    const val GENERAL_ORDER = 100
    const val APPEARANCE_ORDER = 200
    const val ACCOUNTS_ORDER = 400
    const val NOTIFICATIONS_ORDER = 800

    const val PANES_CARD = "arc.panes"
    const val LOOK_CARD = "arc.look"
    const val PULLS_CARD = "arc.notify.pulls"
    const val RUNS_CARD = "arc.notify.runs"
    const val GITHUB_CARD = "arc.github"
    const val UPDATES_CARD = "arc.updates"

    const val THEME = "arc.theme"
    const val SIZE = "arc.size"
    const val WRAP = "arc.wrap"
    const val LINE_HIGHLIGHT = "arc.line-highlight"
    const val ACCOUNT = "arc.account"
    const val TOKEN = "arc.token"
    const val VERSION = "arc.version"
    const val UPDATE = "arc.update"
    const val SWITCH_BACK = "arc.switch-back"
    const val UPDATE_PROBLEM = "arc.update-problem"

    private const val PULL_RULE = "arc.notify.rule."
    private const val RUN_NOTIFY = "arc.notify.run."

    /** The entry of the pull request notification rule [rule], by the rule's id. */
    fun pullRule(rule: String): String = PULL_RULE + rule

    /** The rule id entry [entry] stands for, or null when it is no rule's. */
    fun pullRuleOf(entry: String): String? = entry.takeIf { it.startsWith(PULL_RULE) }?.removePrefix(PULL_RULE)

    fun runNotify(kind: RunNotify): String = RUN_NOTIFY + kind.name

    /** The run notification entry [entry] stands for, or null when it is none's. */
    fun runNotifyOf(entry: String): RunNotify? = entry.takeIf { it.startsWith(RUN_NOTIFY) }?.removePrefix(RUN_NOTIFY)?.let { name -> RunNotify.entries.firstOrNull { it.name == name } }
}
