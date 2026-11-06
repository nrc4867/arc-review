# GitHub API: what Arc can rely on

Read-only spike: official docs, GraphQL schema introspection, GET requests and queries with an OAuth token
(scopes gist, read:org, repo, workflow), and clones of public repos with git 2.45.1. Nothing was written to
GitHub, so every write-side answer is from documentation and schema only. Scripts are in `scripts/`, raw outputs
in `out/`; `scratch/` holds throwaway clones.

## Summary

| # | Question | Answer | Confidence | Exercised |
|---|---|---|---|---|
| 1 | Pending reviews | One per user per PR (implied by the schema; no doc page states it). Read your own with `pullRequest.reviews(states:[PENDING], author:"<login>")`. Write with `addPullRequestReview` (no event, `threads:[…]`), `addPullRequestReviewThread`, `addPullRequestReviewThreadReply`, then `submitPullRequestReview{event, body}`. `addPullRequestReviewComment` and `AddPullRequestReviewInput.comments` are deprecated in their descriptions; `position` is formally deprecated. One call can carry event, body, many threads and multi-line ranges; a suggestion is a ```` ```suggestion ```` block in a comment body. `DraftPullRequestReviewThread` has no `subjectType`, so file-level threads need a separate call. | High for the API shape, medium for one-per-user | Read side and schema |
| 2 | Comments off the diff | The API documents `line` + `side` on lines in the PR diff (changed lines and context lines in hunks) and `subjectType: FILE`. The web UI can comment on any line of a changed file since the new Files changed page; the changelog says API support is limited, so out-of-hunk lines through the API are unverified. No API path accepts a file that is not in the PR. | High for in-hunk and FILE | FILE threads seen live |
| 3 | Viewed state | `viewerViewedState` is VIEWED, UNVIEWED or DISMISSED ("new changes since last viewed"). `markFileAsViewed` / `unmarkFileAsViewed {pullRequestId!, path!}`, one path per call. Fine-grained PATs are reported to get `FORBIDDEN` (community discussion 200353). Whether a rebase with no content change counts as a change is unverified. | High for schema and read | Read |
| 4 | Thread state | Threads expose `isResolved`, `isOutdated`, `isCollapsed`, `resolvedBy`, `subjectType`, `diffSide`, `startDiffSide`, `line`, `originalLine`, `startLine`, `originalStartLine`. Outdated means `line` is null. Moved means `line != originalLine` with `isOutdated` false. LEFT line numbers also move when the base moves. A FILE thread reports `line: 1` while its comment reports null. | High | Yes, on kubernetes PRs |
| 5 | Force-pushes | The timeline has `HeadRefForcePushedEvent{beforeCommit, afterCommit, actor, createdAt}`. With `itemTypes`, read `filteredCount`; `totalCount` ignores the filter. Old head SHAs from 2024 on resolve through GraphQL, REST and `git fetch origin <sha>`; most from before late 2023 are gone. No doc gives a retention period. | High for mechanics, medium for retention | Yes |
| 6 | Search and polling | Limits measured: core 5000/h, graphql 5000/h, search 30/min. A 304 costs nothing. REST search sends no ETag and costs one search request per call. GraphQL `search` costs 1 point and does not touch the search bucket; ten aliased searches in one document cost 1 point in total. `/notifications` sends `X-Poll-Interval: 60` and answers 304. Documented secondary limits: 100 concurrent requests, 900 REST points/min, 2000 GraphQL points/min, 80 writes/min and 500/h. No push channel works without a server. | High | Yes |
| 7 | Checks | REST `commits/{sha}/check-runs`, then `check-runs/{id}/annotations`. For GitHub Actions the check-run id is the job id: `actions/jobs/{id}` gives steps, and `actions/jobs/{id}/logs` redirects to a signed URL valid for one minute, holding one text log for the job split by `##[group]Run` markers. Logs need a token even on public repos. Re-run endpoints need the `repo` scope (docs). Fine-grained PATs cannot call the Checks API (docs). | High | Read, on microsoft/vscode |
| 8 | Commit comments | REST only: `POST commits/{sha}/comments {body, path, position}`. Not shown in PR timelines. | High | Read |
| 9 | Blobless clones | git 2.45.1 has no `git backfill` (2.49 adds it, marked experimental). One tree in one fetch works: `git ls-tree -r <rev>` piped into `git fetch --stdin`. Without a prefetch git fetches blobs one at a time. | High | Yes |

## Blobless clone measurements (cli/cli: 83 MB full, 26,770 blobs in history, 1,545 in the HEAD tree)

| Operation | Time | Lazy fetches |
|---|---|---|
| Blobless clone | 3.7 s (13 MB) | |
| Full clone | 3.7 s (85 MB) | |
| `git grep HEAD`, blobs missing | over 17 min | one per blob |
| `git blame` of one file, blobs missing | 162 s | 296 |
| Batch fetch of the HEAD tree (1,545 blobs) | 1.4 s | 1 |
| `git grep HEAD` after the batch fetch | 0.2 s | 0 |
| Prefetch of a path's history with `log --follow` (313 blobs, both sides of each change) | 3.7 s | |
| `git blame` after that prefetch | 4.6 s | 4 |
| `git blame` after a prefetch without `--follow` | 72 s | 105 |
| Full clone: grep / blame | 0.2 s / 0.1 s | |

Batch tree fetch:

```sh
git ls-tree -r <rev> | awk '$2=="blob"{print $3}' | git -c fetch.negotiationAlgorithm=noop fetch origin \
  --no-tags --no-write-fetch-head --recurse-submodules=no --filter=blob:none --stdin
```

## Design changes recommended

1. **Authentication.** An OAuth token (device flow, or the `gh` login), scopes `repo` and `read:org`. No
   fine-grained PATs: they are reported to fail on `markFileAsViewed`, are barred from the Checks API, and the
   notifications API documents classic tokens only.
2. **Publishing a review.** Keep drafts local. On publish, query `reviews(states:[PENDING], author:me)`. If one
   exists, add to it with `addPullRequestReviewThread{pullRequestReviewId}`; otherwise `addPullRequestReview{threads}`
   with no event. Add FILE threads separately, then `submitPullRequestReview`. Never use `comments`, `position` or
   `addPullRequestReviewComment`. Space mutations at least 1 s apart.
3. **Comment placement.** In-hunk line; else a FILE thread quoting the range with a permalink; else the review
   summary or a PR conversation comment with a permalink. Show any downgrade on the comment itself.
4. **Thread anchoring.** Anchor on `originalCommit` + `originalLine` / `originalStartLine` + side. Use `line` only
   when `isOutdated` is false. Trust comment-level fields over thread-level ones.
5. **Changes since my last review.** Use the last submitted review's `commit.oid` and the force-push events. Fetch
   the reviewed head into a private local ref, because GitHub does not promise to keep it. Expect a null
   `beforeCommit`.
6. **Polling.** Every 90 s, one GraphQL document with all saved filters as aliased `search` fields (`first` ≤ 20):
   1 point, about 40 points/h. A conditional `GET /notifications` every `X-Poll-Interval` (60 s) for new comments,
   @mentions and team mentions. A conditional request or small query for the PR in the foreground. Worst case about
   200 points/h, under 5% of the budget. Never poll REST search. Read the budget from GraphQL `rateLimit`.
7. **Checks.** One rollup of check runs plus commit statuses. For Actions, show the failed step's slice of the job
   log; for other providers link to `details_url`. A failed run can have zero jobs.
8. **Git.** Never let git fetch missing blobs one at a time. Before grep or diff of a revision, batch-fetch that
   tree. Before blame, prefetch the path's `log --follow --raw` blobs, both sides of each change. For small and
   medium repos a full clone costs the same time as a blobless one.

## Search qualifiers for saved filters

`review-requested:@me` includes your teams and drops a PR once you review it; `user-review-requested:@me` covers
direct requests only. Also `team-review-requested:`, `involves:`, `mentions:`, `team:`. Search returns at most
1000 results, and a query takes at most 256 characters and 5 boolean operators.

## Experiments that need write access

Run in a throwaway private repo with a PR from a second branch.

- E1. One pending review per user: call `addPullRequestReview` with no event twice and record the error; repeat
  with a review started in the browser.
- E2. Commentable lines: on a PR changing line 50 of a 200-line file, try `addPullRequestReviewThread` at line 150
  RIGHT, line 150 LEFT, and a path not in the PR; then one `addPullRequestReview` with a good and a bad thread to
  see whether it is atomic.
- E3. Everything in one submission: verdict, body, single-line, multi-line, LEFT-to-RIGHT and suggestion threads,
  plus a FILE thread added to the pending review, then submit.
- E4. Viewed state after a push that touches the file, one that does not, and a rebase with no content change;
  repeat the mark with a fine-grained PAT.
- E5. Force-push, then re-check the old SHA over weeks.
- E6. `rerun-failed-jobs` with the OAuth token.
- E7. A commit comment on a commit in no PR, on a context line and a changed line.
- E8. Whether `addPullRequestReviewThreadReply{pullRequestReviewId}` stays pending until submit.

## Sources

- https://docs.github.com/en/rest/pulls/reviews
- https://docs.github.com/en/rest/pulls/comments
- https://github.blog/changelog/2025-09-25-pull-request-files-changed-public-preview-now-supports-commenting-on-unchanged-lines/
- https://github.blog/changelog/2026-01-22-improved-pull-request-files-changed-page-on-by-default/
- https://github.com/orgs/community/discussions/200353
- https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api
- https://docs.github.com/en/graphql/overview/rate-limits-and-query-limits-for-the-graphql-api
- https://docs.github.com/en/rest/using-the-rest-api/best-practices-for-using-the-rest-api
- https://docs.github.com/en/rest/search/search
- https://docs.github.com/en/rest/activity/notifications
- https://docs.github.com/en/rest/actions/workflow-jobs
- https://docs.github.com/en/rest/actions/workflow-runs
- https://docs.github.com/en/rest/checks/runs
- https://docs.github.com/en/rest/commits/comments
- https://git-scm.com/docs/git-backfill
