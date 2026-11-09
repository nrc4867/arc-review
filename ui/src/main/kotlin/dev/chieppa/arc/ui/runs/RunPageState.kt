package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** What a run's page has picked: the job and step shown, Summary or Jobs once the run is done, and the artifact previewed. */
class RunPageState {
    var job by mutableStateOf<Long?>(null)
    var step by mutableStateOf<Int?>(null)
    var view by mutableStateOf(RunView.Summary)
    var artifact by mutableStateOf<Long?>(null)
    var comparing by mutableStateOf(false)
}
