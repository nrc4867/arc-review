package dev.chieppa.arc.ui.frame

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.review_choose
import dev.chieppa.arc.design.generated.review_new
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.ReviewChoice
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.SearchableDropdown
import org.jetbrains.compose.resources.stringResource

/** The top bar's review selector: the open review's name, every saved review, and a new one at the end. */
@Composable
fun ReviewSelector(workbench: Workbench, modifier: Modifier = Modifier) {
    val current = workbench.state.review
    val newReview = ReviewChoice(null, stringResource(Res.string.review_new))
    val open = ReviewChoice(current.id.value, current.name)
    val options = (listOf(open) + workbench.reviews.filter { it.id != open.id }).sortedBy { it.name.lowercase() } + newReview
    SearchableDropdown(
        options,
        open,
        { if (it != open) workbench.onOpenReview(it.id) },
        { it.name },
        stringResource(Res.string.review_choose),
        modifier.width(ArcSizes.reviewSelector),
    )
}
