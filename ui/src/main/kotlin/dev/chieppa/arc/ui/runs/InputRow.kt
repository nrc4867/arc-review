package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.InputKind
import dev.chieppa.arc.core.actions.WorkflowInput
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.input_boolean
import dev.chieppa.arc.design.generated.input_choice
import dev.chieppa.arc.design.generated.input_environment
import dev.chieppa.arc.design.generated.input_number
import dev.chieppa.arc.design.generated.input_optional
import dev.chieppa.arc.design.generated.input_required
import dev.chieppa.arc.design.generated.input_text
import dev.chieppa.arc.design.generated.input_type
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Segmented
import dev.chieppa.stretch.ui.TextField
import dev.chieppa.stretch.ui.Toggle
import org.jetbrains.compose.resources.stringResource

/**
 * One input of a manual trigger in the Run workflow form: its name over its kind and whether it is required, then a
 * field for text and numbers, choices side by side (a dropdown when there are many), the repo's environments, or a
 * switch with the input's description. A required input left empty is in the error color.
 */
@Composable
fun InputRow(input: WorkflowInput, form: RunWorkflowState) {
    val c = Stretch.colors
    val value = form.values[input.name].orEmpty()
    val set = { text: String -> form.values[input.name] = text }
    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space14), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(RunSizes.formLabel)) {
            RunText(input.name, RunTextKind.Body, c.inkControl)
            val kind = stringResource(
                when (input.kind) {
                    InputKind.Text -> Res.string.input_text
                    InputKind.Number -> Res.string.input_number
                    InputKind.Boolean -> Res.string.input_boolean
                    InputKind.Choice -> Res.string.input_choice
                    InputKind.Environment -> Res.string.input_environment
                },
            )
            val need = if (input.kind == InputKind.Text || input.kind == InputKind.Number) {
                stringResource(Res.string.input_type, kind, stringResource(if (input.required) Res.string.input_required else Res.string.input_optional))
            } else {
                kind
            }
            RunText(need, RunTextKind.MonoSmall, c.inkSubtle)
        }
        Column(Modifier.weight(1f)) {
            val missing = input.required && value.isBlank()
            when (input.kind) {
                InputKind.Text, InputKind.Number -> TextField(
                    value,
                    { text -> set(if (input.kind == InputKind.Number) text.filter { it.isDigit() || it == '.' || it == '-' } else text) },
                    Modifier.fillMaxWidth(),
                    placeholder = input.description.orEmpty(),
                    contentDescription = input.name,
                    error = if (missing) "" else null,
                )
                InputKind.Boolean -> Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                    Toggle(value == TRUE, { set(it.toString()) }, input.name)
                    input.description?.let { RunText(it, RunTextKind.Small, c.inkSubtle) }
                }
                InputKind.Choice -> if (input.options.size <= SIDE_BY_SIDE) {
                    Segmented(input.options, value, set, { it }, mode = true)
                } else {
                    Dropdown(input.options, value, set, { it }, input.name, Modifier.fillMaxWidth())
                }
                InputKind.Environment -> Dropdown(form.environments, value, set, { it }, input.name, Modifier.fillMaxWidth())
            }
        }
    }
}

private const val TRUE = "true"
private const val SIDE_BY_SIDE = 4
