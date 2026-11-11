package dev.chieppa.arc.ui.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.plugins.protocol.AppEventParams
import dev.chieppa.arc.plugins.protocol.AppEvents
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.FileInfo
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Tells running plugins what happens in the window while it is open: a source opening in the strip, a file marked or
 * unmarked, and a push to the host ending.
 */
@Composable
fun PluginEvents(workbench: Workbench) {
    val plugins = workbench.plugins
    if (plugins.manager == null) return
    LaunchedEffect(workbench) {
        var before = workbench.state.workspace.sources.toSet()
        snapshotFlow { workbench.state.workspace.sources }.collect { sources ->
            (sources - before).forEach { plugins.emit(AppEventParams(AppEvents.SOURCE_OPENED, PluginContexts.of(workbench, it))) }
            before = sources.toSet()
        }
    }
    LaunchedEffect(workbench) {
        var before = marksOf(workbench)
        snapshotFlow { marksOf(workbench) }.collect { marks ->
            (marks.keys + before.keys).filter { marks[it] != before[it] }.forEach { (source, path) ->
                val kind = marks[source to path]?.kind?.name?.let(::JsonPrimitive) ?: JsonNull
                val context = CallContext(PluginContexts.info(workbench, source), FileInfo(path))
                plugins.emit(AppEventParams(AppEvents.FILE_MARKED, context, JsonObject(mapOf(AppEvents.KIND to kind))))
            }
            before = marks
        }
    }
    LaunchedEffect(workbench) {
        var before: Source? = null
        snapshotFlow { workbench.comments.pusher.pushing }.collect { pushing ->
            before?.takeIf { pushing == null }?.let { plugins.emit(AppEventParams(AppEvents.PUSH_FINISHED, PluginContexts.of(workbench, it))) }
            before = pushing
        }
    }
}

private fun marksOf(workbench: Workbench): Map<Pair<Source, String>, FileMark> =
    workbench.state.workspace.marks.associateBy { it.source to it.path }
