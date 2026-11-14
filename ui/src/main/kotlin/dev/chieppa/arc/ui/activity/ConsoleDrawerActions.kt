package dev.chieppa.arc.ui.activity

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.activity.ActivityChannel
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.console_all
import dev.chieppa.arc.design.generated.console_all_tip
import dev.chieppa.arc.design.generated.console_channel
import dev.chieppa.arc.design.generated.console_channel_all
import dev.chieppa.arc.design.generated.console_channel_downloads
import dev.chieppa.arc.design.generated.console_channel_git
import dev.chieppa.arc.design.generated.console_channel_github
import dev.chieppa.arc.design.generated.console_clear
import dev.chieppa.arc.design.generated.console_errors
import dev.chieppa.arc.design.generated.console_errors_tip
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.ui.Segmented
import org.jetbrains.compose.resources.stringResource

/** The Console drawer's header controls: the channel shown, All or Errors only, and clearing the channel. */
@Composable
fun ConsoleDrawerActions(console: ActivityConsole) {
    val arc = mapOf(
        ActivityChannel.GIT.id to stringResource(Res.string.console_channel_git),
        ActivityChannel.GITHUB.id to stringResource(Res.string.console_channel_github),
        ActivityChannel.DOWNLOADS.id to stringResource(Res.string.console_channel_downloads),
    )
    val everything = stringResource(Res.string.console_channel_all)
    val options = listOf<String?>(null) + console.channels.map { it.id }
    val titles = console.channels.associate { it.id to (arc[it.id] ?: it.pluginTitle ?: it.id) }
    Dropdown(
        options,
        console.channel,
        { console.channel = it },
        { id -> if (id == null) everything else titles[id] ?: id },
        stringResource(Res.string.console_channel),
        Modifier.width(ArcSizes.consoleChannel),
    )
    val labels = mapOf(ActivityFilter.All to stringResource(Res.string.console_all), ActivityFilter.Errors to stringResource(Res.string.console_errors))
    val tips = mapOf(ActivityFilter.All to stringResource(Res.string.console_all_tip), ActivityFilter.Errors to stringResource(Res.string.console_errors_tip))
    Segmented(
        ActivityFilter.entries,
        console.filter,
        { console.filter = it },
        { labels.getValue(it) },
        compact = true,
        tooltip = { tips[it] },
    )
    TipIconButton(ArcIcons.Delete, stringResource(Res.string.console_clear), console::clear)
}
