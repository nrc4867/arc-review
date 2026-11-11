package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.git.process.GitException
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.gitsource.GitSourceCache
import dev.chieppa.arc.plugins.gitsource.GitSourceState
import dev.chieppa.arc.plugins.gitsource.GitUpdate
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.manifest.PluginManifest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.nio.file.Path

/**
 * The git plugin sources: cloned into [cache], each pinned to the exact commit its ref named when it was added, and
 * checked for new commits when Arc starts and every [PluginServices.gitCheckMillis] after. A new commit is never
 * applied by itself: it waits as the source's [GitUpdate] until the person accepts it, and an update asking for more
 * than was approved keeps its plugin off until the person enables it again. The setup is read through [config] and
 * changed through [save]; [refresh] finds the plugins again, restarting the given source's. One git operation runs at a
 * time, on the source as the setup has it then.
 */
internal class GitSources(
    private val services: PluginServices,
    private val cache: GitSourceCache?,
    private val config: () -> PluginsConfig,
    private val save: (PluginsConfig) -> Unit,
    private val refresh: (PluginSourceSpec?) -> Unit,
    private val manifests: () -> Map<String, PluginManifest>,
) {
    private val known = MutableStateFlow<Map<String, GitSourceState>>(emptyMap())
    private val lock = Mutex()
    private var checking: Job? = null

    /** Each git source's state, by url. */
    val states: StateFlow<Map<String, GitSourceState>> get() = known

    /** The folder [spec]'s plugins run from, once it is cloned and pinned. */
    fun folder(spec: PluginSourceSpec.Git): Path? = cache?.checkout(spec)

    /** The folder [url] is cloned in, whose history a review of an update reads. */
    fun clone(url: String): Path? = cache?.folder(url)

    /** Checks every git source now, and again every [PluginServices.gitCheckMillis]. */
    fun start() {
        if (cache == null || checking != null) return
        checking = services.scope.launch {
            while (isActive) {
                gitSpecs().forEach { check(it.url) }
                delay(services.gitCheckMillis)
            }
        }
    }

    fun stop() {
        checking?.cancel()
        checking = null
    }

    /** Adds the repo at [url] as a source at [ref], cloned and pinned to the commit [ref] names now. */
    fun add(url: String, ref: String) {
        if (cache == null || gitSpecs().any { it.url == url }) return
        val spec = PluginSourceSpec.Git(url, ref)
        save(config().let { it.copy(sources = it.sources + spec) })
        services.scope.launch { check(url) }
    }

    /** Moves [url]'s pin to its waiting update and reloads its plugins. */
    fun accept(url: String) {
        if (known.value[url]?.update == null) return
        services.scope.launch {
            guarded(url) { spec ->
                val update = known.value[url]?.update ?: return@guarded
                cache!!.checkOut(url, update.sha)
                val pinned = replace(spec, spec.copy(pinned = update.sha))
                set(url) { it.copy(update = null) }
                refresh(pinned)
            }
        }
    }

    /** Forgets [spec]'s clone and state. */
    fun remove(spec: PluginSourceSpec.Git) {
        known.update { it - spec.url }
        services.scope.launch { runCatching { cache?.remove(spec.url) } }
    }

    private suspend fun check(url: String) {
        val cache = cache ?: return
        guarded(url) { spec ->
            cache.fetch(spec.url)
            val latest = cache.resolve(spec) ?: throw IOException("$NO_REF ${spec.ref}")
            val pinned = spec.pinned
            if (pinned == null) {
                cache.checkOut(spec.url, latest)
                refresh(replace(spec, spec.copy(pinned = latest)))
                set(spec.url) { it.copy(checkedAt = services.clock(), update = null) }
                return@guarded
            }
            val update = if (latest == pinned) null else updateTo(spec.url, latest)
            set(spec.url) { it.copy(checkedAt = services.clock(), update = update) }
        }
    }

    private suspend fun updateTo(url: String, sha: String): GitUpdate {
        val next = cache!!.manifests(url, sha)
        val now = manifests()
        val asks = next.values.flatMap { manifest ->
            Capability.added(manifest.capabilities, config().approved[manifest.id] ?: now[manifest.id]?.capabilities ?: Capabilities())
        }.distinct()
        return GitUpdate(sha, next.values.firstOrNull()?.version, asks)
    }

    /** Runs [work] on git source [url] as the setup has it now, alone, marking it busy and then whether it failed. */
    private suspend fun guarded(url: String, work: suspend (PluginSourceSpec.Git) -> Unit) = lock.withLock {
        val spec = gitSpecs().firstOrNull { it.url == url } ?: return@withLock
        set(url) { it.copy(busy = true) }
        val failed = try {
            work(spec)
            false
        } catch (_: GitException) {
            true
        } catch (_: IOException) {
            true
        }
        set(url) { it.copy(busy = false, failed = failed) }
    }

    private fun set(url: String, change: (GitSourceState) -> GitSourceState) {
        known.update { it + (url to change(it[url] ?: GitSourceState())) }
    }

    private fun replace(old: PluginSourceSpec.Git, new: PluginSourceSpec.Git): PluginSourceSpec.Git {
        val setup = config()
        save(setup.copy(sources = setup.sources.map { if (it is PluginSourceSpec.Git && it.url == old.url) new else it }))
        return new
    }

    private fun gitSpecs(): List<PluginSourceSpec.Git> = config().sources.filterIsInstance<PluginSourceSpec.Git>()

    private companion object {
        const val NO_REF = "Names no commit:"
    }
}
