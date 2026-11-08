package dev.chieppa.arc.ui.start

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_progress_description
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource

/**
 * A source's files as one bar, in segments: reviewed in the accent, changed since in the warning edge, set aside
 * hatched, and to go as the bare track.
 */
@Composable
fun ReviewBar(counts: ReviewCounts, modifier: Modifier = Modifier.fillMaxWidth()) {
    val c = Stretch.colors
    val description = stringResource(Res.string.start_progress_description, counts.reviewed, counts.counted)
    Canvas(modifier.height(StartSizes.bar).clip(Stretch.shapes.pillShape).semantics { contentDescription = description }) {
        drawRect(c.track)
        if (counts.total == 0) return@Canvas
        val gap = StartSizes.barGap.toPx()
        val hatch = StartSizes.barHatch.toPx()
        var x = 0f
        for ((count, segment) in listOf(counts.reviewed to Segment.Reviewed, counts.changed to Segment.Changed, counts.aside to Segment.Aside)) {
            if (count == 0) continue
            val width = size.width * count / counts.total
            when (segment) {
                Segment.Reviewed -> drawRect(c.accent, Offset(x, 0f), Size(width, size.height))
                Segment.Changed -> drawRect(c.warnEdge, Offset(x, 0f), Size(width, size.height))
                Segment.Aside -> clipRect(x, 0f, x + width, size.height) {
                    var stripe = x - size.height
                    while (stripe < x + width) {
                        drawLine(c.lineStrong, Offset(stripe, size.height), Offset(stripe + size.height, 0f), hatch)
                        stripe += hatch * 2
                    }
                }
            }
            x += width + gap
        }
    }
}

private enum class Segment { Reviewed, Changed, Aside }
