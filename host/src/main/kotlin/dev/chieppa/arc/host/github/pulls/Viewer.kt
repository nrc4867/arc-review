package dev.chieppa.arc.host.github.pulls

/** The person signed in to GitHub: their [login] and the teams they are in, as review requests name them. */
data class Viewer(val login: String, val teams: Set<ReviewRequest.Team> = emptySet()) {
    /** Whether [request] asks this person, directly or through one of their teams. */
    fun isAsked(request: ReviewRequest): Boolean = when (request) {
        is ReviewRequest.User -> request.login.equals(login, ignoreCase = true)
        is ReviewRequest.Team -> teams.any { it.organization.equals(request.organization, true) && it.slug.equals(request.slug, true) }
    }
}
