package dev.chieppa.arc.codeintel

import dev.chieppa.arc.codeintel.provider.DecorationProvider
import dev.chieppa.arc.codeintel.provider.DefinitionProvider
import dev.chieppa.arc.codeintel.provider.HoverProvider
import dev.chieppa.arc.codeintel.provider.OutlineProvider
import dev.chieppa.arc.codeintel.provider.ProviderRegistry
import dev.chieppa.arc.codeintel.provider.UsagesProvider

/**
 * The contribution points for answers about code: one registry per kind of question. Arc's tree-sitter index
 * registers in each it answers; a language server or a plugin registers the same way.
 */
class CodeIntel {
    val definitions = ProviderRegistry<DefinitionProvider>()
    val usages = ProviderRegistry<UsagesProvider>()
    val decorations = ProviderRegistry<DecorationProvider>()
    val hovers = ProviderRegistry<HoverProvider>()
    val outlines = ProviderRegistry<OutlineProvider>()
}
