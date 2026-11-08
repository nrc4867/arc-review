package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import dev.chieppa.arc.ui.services.ConversationService
import dev.chieppa.stretch.ui.decodeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The people's pictures, drawn once each and fetched in the background through [service], which keeps them on disk
 * for a while. Until a picture arrives, or when there is none, an avatar shows initials.
 */
@Stable
class AvatarImages(private val service: ConversationService, private val scope: CoroutineScope) {
    private val images = mutableStateMapOf<String, ImageBitmap>()
    private val asked = mutableSetOf<String>()

    /** The picture at [url], asking for it the first time; null until it arrives. */
    fun image(url: String?): ImageBitmap? {
        if (url.isNullOrBlank()) return null
        images[url]?.let { return it }
        if (asked.add(url)) {
            scope.launch {
                val image = withContext(Dispatchers.Default) { service.avatar(url)?.let(::decodeImage) }
                if (image != null) images[url] = image
            }
        }
        return null
    }
}
