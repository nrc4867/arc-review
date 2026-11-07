package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_refs_more
import dev.chieppa.arc.design.generated.switcher_refs_two
import org.jetbrains.compose.resources.stringResource

/** Refs named in a sentence: `api#412`, `api#412 and web#88`, or `api#412, web#88 and 2 more`. */
@Composable
fun refsText(refs: List<String>): String = when (refs.size) {
    0 -> ""
    1 -> refs[0]
    2 -> stringResource(Res.string.switcher_refs_two, refs[0], refs[1])
    else -> stringResource(Res.string.switcher_refs_more, refs[0], refs[1], refs.size - 2)
}
