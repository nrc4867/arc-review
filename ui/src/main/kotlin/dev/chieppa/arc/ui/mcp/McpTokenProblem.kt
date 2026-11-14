package dev.chieppa.arc.ui.mcp

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.mcp_token_keychain
import dev.chieppa.arc.design.generated.mcp_token_unavailable
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource

/**
 * Why the MCP server's token is not kept in the keychain, or, when [unavailable], why there is no token at all: the
 * [reason] in the error ink, selectable to copy.
 */
@Composable
fun McpTokenProblem(reason: String, unavailable: Boolean) {
    val text = stringResource(if (unavailable) Res.string.mcp_token_unavailable else Res.string.mcp_token_keychain, reason)
    SelectionContainer {
        BasicText(
            text,
            Modifier.testTag(McpSettingsTags.TOKEN_PROBLEM),
            style = Stretch.type.bodySm.merge(TextStyle(color = Stretch.colors.errorInk)),
            maxLines = PROBLEM_LINES,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** How many lines of a token problem show before it is cut short. */
private const val PROBLEM_LINES = 4
