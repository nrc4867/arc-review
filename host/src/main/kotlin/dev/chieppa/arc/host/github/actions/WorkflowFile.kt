package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.InputKind
import dev.chieppa.arc.core.actions.WorkflowInput
import dev.chieppa.arc.core.actions.WorkflowJobSpec
import dev.chieppa.arc.core.actions.WorkflowSpec
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.exceptions.YamlEngineException

/**
 * Reads what the app uses from a workflow file: its name, its `workflow_dispatch` inputs and its jobs with what they
 * need. YAML 1.2 is read, so `on` is a key and not `true`; only plain maps, lists and scalars are built.
 */
object WorkflowFile {
    private const val DISPATCH = "workflow_dispatch"

    /** The workflow in [text], or null when it is not YAML or not a map. */
    fun parse(text: String): WorkflowSpec? {
        val root = try {
            Load(LoadSettings.builder().setAllowDuplicateKeys(true).setMaxAliasesForCollections(MAX_ALIASES).build()).loadFromString(text)
        } catch (_: YamlEngineException) {
            return null
        } as? Map<*, *> ?: return null
        return WorkflowSpec(root["name"]?.toString(), dispatch(root["on"]), jobs(root["jobs"]))
    }

    private fun dispatch(on: Any?): List<WorkflowInput>? = when (on) {
        is String -> if (on == DISPATCH) emptyList() else null
        is List<*> -> if (DISPATCH in on) emptyList() else null
        is Map<*, *> -> if (on.containsKey(DISPATCH)) inputs((on[DISPATCH] as? Map<*, *>)?.get("inputs")) else null
        else -> null
    }

    private fun inputs(inputs: Any?): List<WorkflowInput> = (inputs as? Map<*, *>).orEmpty().mapNotNull { (name, value) ->
        val spec = value as? Map<*, *> ?: emptyMap<Any, Any>()
        val kind = when (spec["type"]?.toString()) {
            "number" -> InputKind.Number
            "boolean" -> InputKind.Boolean
            "choice" -> InputKind.Choice
            "environment" -> InputKind.Environment
            else -> InputKind.Text
        }
        WorkflowInput(
            name = name?.toString() ?: return@mapNotNull null,
            kind = kind,
            description = spec["description"]?.toString(),
            required = spec["required"] == true || spec["required"]?.toString() == "true",
            default = spec["default"]?.toString(),
            options = (spec["options"] as? List<*>).orEmpty().map { it.toString() },
        )
    }

    private fun jobs(jobs: Any?): List<WorkflowJobSpec> = (jobs as? Map<*, *>).orEmpty().mapNotNull { (id, value) ->
        val job = value as? Map<*, *> ?: emptyMap<Any, Any>()
        val needs = when (val n = job["needs"]) {
            is String -> listOf(n)
            is List<*> -> n.map { it.toString() }
            else -> emptyList()
        }
        WorkflowJobSpec(id?.toString() ?: return@mapNotNull null, job["name"]?.toString(), needs)
    }

    private const val MAX_ALIASES = 50
}
