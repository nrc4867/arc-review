package dev.chieppa.arc.ui.install

import dev.chieppa.arc.install.InstallStep

/** Test tags of the install window's rows, naming the step and where it stands, and of a failed step's reason. */
object InstallTags {
    fun row(step: InstallStep, status: StepStatus): String = "install.${step.name}.${status.name}"

    fun reason(step: InstallStep): String = "install.${step.name}.reason"
}
