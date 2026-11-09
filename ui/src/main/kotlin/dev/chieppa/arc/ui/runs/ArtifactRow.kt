package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.artifact_expired
import dev.chieppa.arc.design.generated.artifact_expires
import dev.chieppa.arc.design.generated.artifact_save
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource
import java.time.Duration
import java.time.Instant

/** One artifact of a run: its name, the job that made it and when it expires, its size, and a save button. */
@Composable
fun ArtifactRow(artifact: RunArtifact, job: String?, selected: Boolean, onClick: () -> Unit, onSave: () -> Unit) {
    val c = Stretch.colors
    val edge = RunSizes.selectedEdge
    Row(
        Modifier.fillMaxWidth().heightIn(min = RunSizes.artifactRow)
            .background(if (selected) c.onFill else Color.Transparent)
            .drawBehind { if (selected) drawLine(c.accent, Offset(edge.toPx() / 2, 0f), Offset(edge.toPx() / 2, size.height), edge.toPx()) }
            .bottomRule(c.chartGrid).clickable(enabled = !artifact.expired, role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }.padding(horizontal = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(ArcIcons.Artifact, if (artifact.expired) c.inkSubtle else c.inkMuted)
        Column(Modifier.weight(1f)) {
            RunText(artifact.name, RunTextKind.Strong, if (artifact.expired) c.inkSubtle else c.inkStrong)
            val expiry = if (artifact.expired) {
                stringResource(Res.string.artifact_expired)
            } else {
                artifact.expiresAt?.let { stringResource(Res.string.artifact_expires, Duration.between(Instant.now(), it).toDays().coerceAtLeast(0)) }.orEmpty()
            }
            RunText(listOfNotNull(job, expiry.ifEmpty { null }).joinToString(SEPARATOR), RunTextKind.Small, c.inkSubtle)
        }
        RunText(Sizes.bytes(artifact.sizeBytes), RunTextKind.MonoSmall, c.inkMuted)
        if (!artifact.expired) TipIconButton(ArcIcons.Download, stringResource(Res.string.artifact_save, artifact.name), onSave)
    }
}

private const val SEPARATOR = " · "
