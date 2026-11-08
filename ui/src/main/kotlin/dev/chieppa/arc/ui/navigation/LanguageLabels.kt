package dev.chieppa.arc.ui.navigation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.language_java
import dev.chieppa.arc.design.generated.language_javascript
import dev.chieppa.arc.design.generated.language_kotlin
import dev.chieppa.arc.design.generated.language_tsql
import dev.chieppa.arc.design.generated.language_tsx
import dev.chieppa.arc.design.generated.language_typescript
import org.jetbrains.compose.resources.stringResource

/** A language id as answers carry it, by its name. An id Arc does not know shows as it is. */
@Composable
fun languageLabel(id: String): String = when (id) {
    "kotlin" -> stringResource(Res.string.language_kotlin)
    "java" -> stringResource(Res.string.language_java)
    "javascript" -> stringResource(Res.string.language_javascript)
    "typescript" -> stringResource(Res.string.language_typescript)
    "tsx" -> stringResource(Res.string.language_tsx)
    "tsql" -> stringResource(Res.string.language_tsql)
    else -> id
}
