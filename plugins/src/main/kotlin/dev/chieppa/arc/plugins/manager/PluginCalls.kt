package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.console.ConsoleProcess
import dev.chieppa.arc.plugins.manifest.PluginManifest
import dev.chieppa.arc.plugins.protocol.ConsoleExitParams
import dev.chieppa.arc.plugins.protocol.ConsoleHandleParams
import dev.chieppa.arc.plugins.protocol.ConsoleOutputParams
import dev.chieppa.arc.plugins.protocol.ConsoleRunParams
import dev.chieppa.arc.plugins.protocol.ConsoleRunResult
import dev.chieppa.arc.plugins.protocol.ConsoleWriteParams
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.FetchParams
import dev.chieppa.arc.plugins.protocol.FetchResult
import dev.chieppa.arc.plugins.protocol.FilesReadParams
import dev.chieppa.arc.plugins.protocol.FilesReadResult
import dev.chieppa.arc.plugins.protocol.FilesWriteParams
import dev.chieppa.arc.plugins.protocol.GitGrepParams
import dev.chieppa.arc.plugins.protocol.GitGrepResult
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.NoteCreateParams
import dev.chieppa.arc.plugins.protocol.NoteInfo
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.NotesListParams
import dev.chieppa.arc.plugins.protocol.NotesListResult
import dev.chieppa.arc.plugins.protocol.PanesOpenParams
import dev.chieppa.arc.plugins.protocol.ProtocolJson
import dev.chieppa.arc.plugins.protocol.RpcError
import dev.chieppa.arc.plugins.protocol.RpcException
import dev.chieppa.arc.plugins.protocol.SettingsSetParams
import dev.chieppa.arc.plugins.protocol.StoreGetParams
import dev.chieppa.arc.plugins.protocol.StoreSetParams
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Answers one plugin's calls into Arc, each checked against its manifest's capabilities first: its settings and
 * private store, the processes it runs, its panes, git searches, files, HTTP requests, and the person's notes and
 * comment drafts. [folder] is the plugin's own; [settings] and [setSettings] read and write its stored settings; [notify] sends it a notification.
 */
internal class PluginCalls(
    private val manifest: () -> PluginManifest,
    private val folder: () -> Path,
    private val services: PluginServices,
    private val settings: () -> JsonObject,
    private val setSettings: (JsonObject) -> Unit,
    private val notify: suspend (String, JsonElement) -> Unit,
) {
    private val id: String get() = manifest().id
    private val files = FileCalls(folder) { manifest().capabilities }
    private val fetch = FetchCalls(services.fetcher) { manifest().capabilities }
    private val notes = NoteCalls { services.ui.notes }

    suspend fun answer(method: String, params: JsonElement): JsonElement {
        CapabilityGate.check(method, manifest().capabilities)
        return when (method) {
            Methods.SETTINGS_SET -> read(SettingsSetParams.serializer(), params).let { setSettings(JsonObject(settings() + (it.key to it.value))); JsonNull }
            Methods.STORE_GET -> services.storage.loadStore(id)[read(StoreGetParams.serializer(), params).key] ?: JsonNull
            Methods.STORE_SET -> read(StoreSetParams.serializer(), params).let { store(it.key, it.value) }
            Methods.STORE_DELETE -> store(read(StoreGetParams.serializer(), params).key, null)
            Methods.CONSOLE_RUN -> run(read(ConsoleRunParams.serializer(), params))
            Methods.CONSOLE_WRITE -> read(ConsoleWriteParams.serializer(), params).let { process(it.handle).write(it.text); JsonNull }
            Methods.CONSOLE_KILL -> process(read(ConsoleHandleParams.serializer(), params).handle).kill().let { JsonNull }
            Methods.PANES_OPEN -> services.ui.openPane(id, read(PanesOpenParams.serializer(), params)).let { JsonNull }
            Methods.GIT_GREP -> grep(read(GitGrepParams.serializer(), params))
            Methods.FILES_READ -> ProtocolJson.toElement(FilesReadResult.serializer(), files.read(read(FilesReadParams.serializer(), params)))
            Methods.FILES_WRITE -> files.write(read(FilesWriteParams.serializer(), params)).let { JsonNull }
            Methods.NET_FETCH -> ProtocolJson.toElement(FetchResult.serializer(), fetch.fetch(read(FetchParams.serializer(), params)))
            Methods.NOTES_LIST -> ProtocolJson.toElement(NotesListResult.serializer(), notes.list(read(NotesListParams.serializer(), params)))
            Methods.NOTES_CREATE -> ProtocolJson.toElement(NoteInfo.serializer(), notes.add(NoteKind.Note, read(NoteCreateParams.serializer(), params)))
            Methods.COMMENTS_DRAFT -> ProtocolJson.toElement(NoteInfo.serializer(), notes.add(NoteKind.Draft, read(NoteCreateParams.serializer(), params)))
            else -> throw RpcException(RpcError(ErrorCodes.UNKNOWN_METHOD, method))
        }
    }

    private fun <T> read(serializer: KSerializer<T>, params: JsonElement): T = ProtocolJson.fromElement(serializer, params)

    @Synchronized
    private fun store(key: String, value: JsonElement?): JsonElement {
        val stored = services.storage.loadStore(id)
        services.storage.saveStore(id, JsonObject(if (value == null) stored - key else stored + (key to value)))
        return JsonNull
    }

    private fun run(params: ConsoleRunParams): JsonElement {
        val dir = when (params.cwd) {
            ConsoleRunParams.WORKTREE -> params.context.source?.worktree?.let(Path::of) ?: folder()
            ConsoleRunParams.PLUGIN -> folder()
            else -> Path.of(params.cwd)
        }
        if (params.command.isEmpty() || !Files.isDirectory(dir)) throw failed("$NO_FOLDER $dir")
        val process = try {
            services.consoles.start(id, params.command, dir, ::printed, ::exited)
        } catch (e: IOException) {
            throw failed(e.message ?: e.toString())
        }
        return ProtocolJson.toElement(ConsoleRunResult.serializer(), ConsoleRunResult(process.id))
    }

    private fun printed(process: ConsoleProcess, chunk: dev.chieppa.arc.plugins.console.ConsoleChunk) {
        services.scope.launch {
            notify(Methods.CONSOLE_OUTPUT, ProtocolJson.toElement(ConsoleOutputParams.serializer(), ConsoleOutputParams(process.id, chunk.text, chunk.error)))
        }
    }

    private fun exited(process: ConsoleProcess, code: Int) {
        services.scope.launch { notify(Methods.CONSOLE_EXIT, ProtocolJson.toElement(ConsoleExitParams.serializer(), ConsoleExitParams(process.id, code))) }
    }

    private fun process(handle: String): ConsoleProcess =
        services.consoles[handle]?.takeIf { it.plugin == id } ?: throw failed("$NO_PROCESS $handle")

    private suspend fun grep(params: GitGrepParams): JsonElement {
        val source = params.context.source ?: throw failed(NO_SOURCE)
        val worktree = source.worktree?.let(Path::of) ?: throw failed(NO_SOURCE)
        val hits = services.grep.grep(worktree, params.pattern, source.head, params.limit)
        return ProtocolJson.toElement(GitGrepResult.serializer(), GitGrepResult(hits))
    }

    private fun failed(message: String) = RpcException(RpcError(ErrorCodes.FAILED, message))

    private companion object {
        const val NO_FOLDER = "No such folder:"
        const val NO_PROCESS = "No such process:"
        const val NO_SOURCE = "No source with a clone here"
    }
}
