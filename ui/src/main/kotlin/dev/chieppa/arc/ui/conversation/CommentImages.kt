package dev.chieppa.arc.ui.conversation

import androidx.compose.ui.graphics.ImageBitmap
import dev.chieppa.arc.ui.services.ConversationService
import dev.chieppa.stretch.markdown.ImageSource
import dev.chieppa.stretch.ui.decodeImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Collections

/**
 * The pictures in comments, fetched through [service] and drawn on [work] when a comment shows them, the latest
 * [KEPT] kept in memory. One that is not a picture, such as a video, or cannot be had, is null.
 */
class CommentImages(private val service: ConversationService, private val work: CoroutineDispatcher) : ImageSource {
    private val kept: MutableMap<String, ImageBitmap> = Collections.synchronizedMap(object : LinkedHashMap<String, ImageBitmap>(KEPT, LOAD, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>): Boolean = size > KEPT
    })

    override suspend fun load(url: String): ImageBitmap? {
        kept[url]?.let { return it }
        val image = withContext(work) { service.picture(url)?.let { runCatching { decodeImage(it) }.getOrNull() } } ?: return null
        kept[url] = image
        return image
    }

    private companion object {
        const val KEPT = 64
        const val LOAD = 0.75f
    }
}
