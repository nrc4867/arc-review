package dev.chieppa.arc.ui.mcp

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/** Pictures the window draws, as PNG bytes. */
object PngImages {
    /** [bitmap] encoded as PNG; null when Skia cannot encode it. */
    fun encode(bitmap: ImageBitmap): ByteArray? = Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
}
