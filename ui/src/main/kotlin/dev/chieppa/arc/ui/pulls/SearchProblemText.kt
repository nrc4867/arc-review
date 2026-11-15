package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filters_problem_empty
import dev.chieppa.arc.design.generated.filters_problem_empty_qualifier
import dev.chieppa.arc.design.generated.filters_problem_no_query
import dev.chieppa.arc.design.generated.filters_problem_not_github
import dev.chieppa.arc.design.generated.filters_problem_not_pulls
import dev.chieppa.arc.design.generated.filters_problem_not_search
import dev.chieppa.arc.design.generated.filters_problem_parentheses
import dev.chieppa.arc.design.generated.filters_problem_unclosed_quote
import dev.chieppa.arc.host.github.search.SearchProblem
import dev.chieppa.arc.host.github.search.SearchReading
import org.jetbrains.compose.resources.stringResource

/** Why [refused] makes no filter, in a sentence for its field's tooltip and the Console. */
@Composable
fun searchProblemText(refused: SearchReading.Refused): String {
    val detail = refused.detail.orEmpty()
    return when (refused.problem) {
        SearchProblem.Empty -> stringResource(Res.string.filters_problem_empty)
        SearchProblem.NotGitHub -> stringResource(Res.string.filters_problem_not_github)
        SearchProblem.NotASearch -> stringResource(Res.string.filters_problem_not_search)
        SearchProblem.NoQuery -> stringResource(Res.string.filters_problem_no_query)
        SearchProblem.NotPullRequests -> stringResource(Res.string.filters_problem_not_pulls, detail)
        SearchProblem.UnclosedQuote -> stringResource(Res.string.filters_problem_unclosed_quote)
        SearchProblem.UnbalancedParentheses -> stringResource(Res.string.filters_problem_parentheses)
        SearchProblem.EmptyQualifier -> stringResource(Res.string.filters_problem_empty_qualifier, detail)
    }
}
