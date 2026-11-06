package dev.chieppa.arc.ui.status

import dev.chieppa.stretch.code.CodeLanguage

/** A language's name as the status bar shows it: its id with a capital, `kotlin` as `Kotlin`. */
object LanguageNames {
    fun of(language: CodeLanguage): String = language.id.replaceFirstChar { it.uppercaseChar() }
}
