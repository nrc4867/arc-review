package dev.chieppa.arc.index.resolve

/** JavaScript and TypeScript relative imports: `./ui/Button` from `src/app/App.tsx` is `src/app/ui/Button.tsx` or its `index`. */
object ModulePaths {
    private const val INDEX = "/index"

    fun resolvesTo(fromPath: String, module: String, candidatePath: String): Boolean {
        if (!module.startsWith(".")) return false
        val parts = ArrayList(fromPath.substringBeforeLast('/', "").split('/').filter { it.isNotEmpty() })
        for (segment in module.split('/')) {
            when (segment) {
                ".", "" -> Unit
                ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.size - 1)
                else -> parts += segment
            }
        }
        val target = parts.joinToString("/")
        val stem = candidatePath.substringBeforeLast('.')
        return stem == target || stem == target + INDEX || candidatePath == target
    }
}
