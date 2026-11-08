package dev.chieppa.arc.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import dev.chieppa.arc.core.start.StartRow
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ControlLabel

/**
 * One start row: its icon, title and subtitle, and its trailing item; a row that opens a web page ends in the
 * external-link mark. A row whose action can run takes clicks; a right-click offers its menu items.
 */
@Composable
fun StartRowView(workbench: Workbench, row: StartRow) {
    val c = Stretch.colors
    val action = row.action?.takeIf { StartRunner.available(workbench, it) }
    val hover = remember { MutableInteractionSource() }
    val hovered by hover.collectIsHoveredAsState()
    val press = if (action == null) Modifier else Modifier
        .background(if (hovered) c.surfaceControl else Color.Transparent)
        .hoverable(hover)
        .clickable(hover, null, role = Role.Button) { StartRunner.run(workbench, action) }
    val body = @Composable {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = if (row.subtitle == null) StartSizes.rowSmall else StartSizes.row)
                .then(press)
                .padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StartIcons.of(row.icon)?.let { Icon(it, c.inkControl) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2 / 2)) {
                ControlLabel(row.title, Stretch.type.body, if (row.subtitle == null) c.ink else c.inkStrong)
                row.subtitle?.let { ControlLabel(it, Stretch.type.bodySm, c.inkMuted) }
            }
            row.trailing?.let { StartTrailingView(workbench, it) }
            if (action?.id == ArcActionIds.OPEN_LINK.value) Icon(StretchIcons.ExternalLink, c.inkSubtle, size = ArcSizes.smallIcon)
        }
    }
    if (row.menu.isEmpty()) body() else ContextMenuArea({ close -> StartMenuItems(workbench, row.menu, close) }) { body() }
}
