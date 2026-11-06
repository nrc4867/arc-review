package dev.chieppa.arc.build

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Writes Compose vector icons from the 16px stroke SVGs in design/icons. Supports path, circle and rect elements; an
 * element with fill="currentColor" is drawn filled, everything else stroked. With [verify] set, the task fails when
 * the checked-in file differs from what the SVGs produce instead of writing it.
 */
abstract class GenerateIcons : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val icons: DirectoryProperty

    @get:Internal
    abstract val target: RegularFileProperty

    @get:Input
    abstract val verify: Property<Boolean>

    init {
        verify.convention(false)
    }

    @TaskAction
    fun run() {
        val file = target.get().asFile
        val text = render()
        if (verify.get()) {
            if (!file.exists() || file.readText().replace("\r\n", "\n") != text) {
                throw GradleException("${file.name} is out of date; run :design:generateIcons")
            }
        } else {
            file.parentFile.mkdirs()
            file.writeText(text)
        }
    }

    private fun render(): String {
        val files = icons.get().asFile.listFiles { f -> f.extension == "svg" }!!.sortedBy { it.name }
        val out = StringBuilder()
        out.append("// Generated from design/icons by :design:generateIcons. Do not edit.\n")
        out.append("package dev.chieppa.arc.design\n\n")
        out.append("import androidx.compose.ui.graphics.vector.ImageVector\n\n")
        out.append("/** Arc's own icons, for Arc's own functions, on Stretch's 16px grid. */\n")
        out.append("object ArcIcons {\n")
        for (file in files) {
            val name = file.nameWithoutExtension
            out.append("    val ${pascalCase(name)}: ImageVector by lazy {\n")
            out.append("        icon(\"$name\") {\n")
            val children = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement.childNodes
            for (i in 0 until children.length) {
                val el = children.item(i) as? Element ?: continue
                val d = when (el.tagName) {
                    "path" -> el.getAttribute("d")
                    "circle" -> circle(el.num("cx"), el.num("cy"), el.num("r"))
                    "rect" -> rect(el.num("x"), el.num("y"), el.num("width"), el.num("height"), el.num("rx"))
                    else -> error("${file.name}: unsupported element <${el.tagName}>")
                }
                val kind = if (el.getAttribute("fill") == "currentColor") "fill" else "stroke"
                out.append("            $kind(\"$d\")\n")
            }
            out.append("        }\n    }\n")
        }
        out.append("}\n")
        return out.toString()
    }

    private fun pascalCase(name: String) =
        name.split('-').joinToString("") { part -> part.replaceFirstChar { it.uppercase() } }

    private fun Element.num(name: String) = getAttribute(name).ifEmpty { "0" }.toDouble()

    private fun circle(cx: Double, cy: Double, r: Double) =
        "M${n(cx - r)} ${n(cy)}a${n(r)} ${n(r)} 0 1 0 ${n(2 * r)} 0a${n(r)} ${n(r)} 0 1 0 ${n(-2 * r)} 0z"

    private fun rect(x: Double, y: Double, w: Double, h: Double, rx: Double): String {
        if (rx <= 0.0) return "M${n(x)} ${n(y)}h${n(w)}v${n(h)}h${n(-w)}z"
        val r = minOf(rx, w / 2, h / 2)
        return "M${n(x + r)} ${n(y)}h${n(w - 2 * r)}a${n(r)} ${n(r)} 0 0 1 ${n(r)} ${n(r)}v${n(h - 2 * r)}" +
            "a${n(r)} ${n(r)} 0 0 1 ${n(-r)} ${n(r)}h${n(-(w - 2 * r))}a${n(r)} ${n(r)} 0 0 1 ${n(-r)} ${n(-r)}" +
            "v${n(-(h - 2 * r))}a${n(r)} ${n(r)} 0 0 1 ${n(r)} ${n(-r)}z"
    }

    private fun n(value: Double): String {
        val rounded = Math.round(value * 1000) / 1000.0
        return if (rounded == Math.floor(rounded)) rounded.toLong().toString() else rounded.toString()
    }
}
