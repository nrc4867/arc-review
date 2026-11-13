package dev.chieppa.arc.ui.install

import dev.chieppa.arc.install.InstallStep

/** Test tags of the install window's rows, naming the step and where it stands. */
object InstallTags {
    fun row(step: InstallStep, status: StepStatus): String = "install.${step.name}.${status.name}"
}
