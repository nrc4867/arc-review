# Reviewed marks: fingerprint and "changes since my last review"

Environment: git 2.45.1.windows.1, Windows 11, Git Bash. Anything not run is marked (unverified).

## How to run (Git Bash, from the spike folder `arc-review-spikes/mark-hashing`, kept outside this repo)

- Scenario table: `bash scripts/run_scenarios.sh > results/scenario-table.md` (about 5 minutes; rebuilds `work/scen/*`).
- "Changes since my last review" views: `bash scripts/since_review.sh > results/since-review.txt`.
- Performance: `bash scripts/perf.sh > results/perf.txt` (first run clones git/git into `cache/`).
- `scripts/lib/env.sh` pins a hermetic git environment and the diff options; `fp.pl` and `fp_all.pl` are the
  reference fingerprint; `fingerprints.sh` holds candidates A–F and the replay; `scenarios.sh` holds the 25 scenarios.

## 1. Recommendation

Store a hash of the added and removed lines as the file's identity, but do not decide keep or clear by that hash
alone. Decide by replaying what was reviewed onto the new merge-base and comparing the result with the new head.
Fall back to the hash only when that replay conflicts.

### 1.1 What a mark stores

- `repo`, `pr`, `key`: the file's path after the change; for a deleted file, the path before.
- `H0`: the reviewed head commit, pinned locally as `refs/arc/reviewed/<pr>/<H0>` so it survives force-pushes and gc.
- `M0`: `merge-base(base, H0)` at review time.
- `metaHash`, `fpExact`, `hunkHashes[]` as defined below.

### 1.2 The fingerprint (candidate B)

Computed from the three-dot diff, `M = merge-base(base, head)` .. `head`.

```
DIFF = git -c core.quotePath=false diff --no-ext-diff --no-textconv --no-color --histogram
       --full-index -M50% -U0 --src-prefix=a/ --dst-prefix=b/ M head
       // pin every option: user config (diff.algorithm, renames, textconv, external diff) must not change the identity
per file section:
  key = the b/ path (the a/ path if the file was deleted)
  header lines (before the first @@):
    a line matching ^(old mode|new mode|new file mode|deleted file mode|rename from|copy from)<SP>
        -> append verbatim to meta
    "index A..B"       -> remember A, B
    "Binary files ..." -> append "binary A B" to meta
    ignored: similarity index, rename to, ---/+++
  each "@@" starts a hunk. Header text, line numbers and function context are NOT hashed.
  hunk body: keep lines starting with '+', '-' or '\' as raw bytes, without the LF (a CR stays).
             Drop ' ' context lines.
  metaHash   = sha256("M\n" + join("\n", meta))
  fpExact    = sha256(join("\n", ["M", join("\n", meta)] ++ concat(["@@"] ++ h for each non-empty hunk h)))
  hunkHashes = [sha256(join("\n", h)) for each hunk h]
```

No whitespace normalisation. In production take paths from `git diff --raw -z`; the spike parses `diff --git a/ b/`
headers, which only works for ordinary paths.

### 1.3 Keep or clear (candidate F), once per new head H1 with base B1

```
M1 = merge-base(B1, H1)
(T, conflicted) = git merge-tree --write-tree --name-only -z --merge-base=M0 M1 H0
    // the reviewed PR replayed onto the new merge-base; exit 1 = some paths conflicted
new = fingerprints of diff(M1, H1)        // one process

for each mark:
  key not in new                    -> CLEAR
  new.metaHash != mark.metaHash     -> CLEAR
  H0 or M0 not available            -> KEEP iff new.fpExact == mark.fpExact      (degraded mode)
  key not in conflicted             -> KEEP iff mode and oid of T:key == mode and oid of H1:key
  key in conflicted                 -> KEEP iff new.fpExact == mark.fpExact
```

Fast path: if `M1 == M0` and the file's blob is the same in H1 as in H0, keep without running the replay.

The scenario table computed the replay per file with `git merge-file --object-id --diff-algorithm=histogram`. The
whole-PR `merge-tree` form matched for scenarios b, c, d, e, g and k but has not been run against the full table.

### 1.4 "Changes since my last review"

Use the same replayed tree `T`:

- Conflicted file: show `diff(T:f, H1:f)`, the conflict markers against the author's resolution.
- `T:f == H1:f`: unchanged since the review; collapse it.
- File new to the PR: show the full `M1..H1` diff.
- Anything else: show `diff(T:f, H1:f)`, which is exactly what the author changed after the review. Base movement
  cancels out, and so does any part of the PR that landed upstream.

## 2. Candidates

- A: `git patch-id --stable` of the `-U3` patch (includes context; ignores whitespace and line numbers).
- B: exact added and removed lines plus meta (1.2).
- C: B over a `-w --ignore-blank-lines` diff with whitespace collapsed.
- D: metaHash plus the multiset of per-hunk hashes; keep iff the new hunks are a subset of the old.
- E: replay only; a conflict means clear.
- F: meta must match, then E, then B as the conflict fallback. Recommended.

`git range-diff` is rejected as a fingerprint: it pairs commits, not files, so squash and split break it, and with
the default creation factor it did not pair the unchanged commit in scenario b.

## 3. Results

`(X)` marks a wrong verdict.

| id | scenario | right | A | B | C | D | E | F |
|---|---|---|---|---|---|---|---|---|
| a | rebase, base changed another file | keep | keep | keep | keep | keep | keep | keep |
| a2 | rebase, base edited same file far from the hunk | keep | keep | keep | keep | keep | keep | keep |
| b | rebase, base edited lines inside the -U3 context, clean | keep | clear (X) | keep | keep | keep | keep | keep |
| b2 | rebase, base edited the line touching the hunk, mechanical conflict resolution | keep | clear (X) | keep | keep | keep | clear (X) | keep |
| c | base changed the same line, author resolved the conflict | clear | clear | clear | clear | clear | clear | clear |
| d | amend one hunk of three | clear | clear | clear | clear | clear | clear | clear |
| e | new hunk added to a reviewed file | clear | clear | clear | clear | clear | clear | clear |
| f | squash, same tree | keep | keep | keep | keep | keep | keep | keep |
| g | base merged into the PR | keep | keep | keep | keep | keep | keep | keep |
| g3 | base merged into the PR with a conflict on the PR's line | clear | clear | clear | clear | clear | clear | clear |
| h1 | PR is a pure rename; rebase onto a base that edited the file | keep | keep | keep | keep | keep | keep | keep |
| h2 | rename plus edit; rebase | keep | keep | keep | keep | keep | keep | keep |
| h3 | reviewed edit, then the author renames the file | clear | clear | clear | clear | clear | clear | clear |
| i1 | author converts the whole file to CRLF | clear | clear | clear | keep (X) | clear | clear | clear |
| i2 | author re-indents only their own added lines | clear | keep (X) | clear | keep (X) | clear | clear | clear |
| i3 | base converted the file to CRLF; author re-applied the same edit | keep | keep | clear (X) | keep | clear (X) | clear (X) | clear (X) |
| j1 | same line added in two places; author removes one | clear | clear | clear | clear | keep (X) | clear | clear |
| j2 | identical added line moved to another place | clear | clear | keep (X) | keep (X) | keep (X) | clear | clear |
| k | one of the PR's commits was cherry-picked upstream; author rebases | keep | clear (X) | clear (X) | clear (X) | keep | keep | keep |
| l1 | binary added; rebase | keep | keep | keep | keep | keep | keep | keep |
| l2 | binary changed after review | clear | clear | clear | clear | clear | clear | clear |
| m | mode change only (+x) after review | clear | clear | clear | clear | clear | keep (X) | clear |
| n1 | file added; rebase | keep | keep | keep | keep | keep | keep | keep |
| n2 | file deleted; rebase | keep | keep | keep | keep | keep | keep | keep |
| o | PR moves a 5-line block; base edits near the destination; rebase | keep | clear (X) | keep | keep | keep | keep | keep |

Wrong verdicts: A 6, B 3, C 5, D 4, E 3, F 1. F's one miss (i3) is a false clear, the safe direction.

Marks are keyed by path, so after a rename (h3) the new path has no mark. Carrying a mark across a rename is
possible, since the added and removed lines are unchanged, but is not implemented.

## 4. Views compared for "changes since my last review"

V1 = `git diff H0 H1`; V2 = `range-diff M0..H0 M1..H1`; V3 = interdiff of the two PR diffs; V4 = `git diff T H1`.

- b (base edited the context): V1 shows the base's edits as PR changes; V2 fails to pair the commit at default
  settings; V3 shows header noise; V4 is empty.
- c (conflict resolved): V1 hides that the base changed the line; V2 and V3 are correct but hard to read; V4 shows
  the conflict markers (base against PR) beside the resolution.
- d (one hunk amended): V2 reports a dropped commit plus a new one; V4 is the cleanest.
- e (new hunk): V4 shows the new hunk only.
- g (base merged in): V1 shows base noise; V4 is empty.
- k (part landed upstream): V3 makes the landed change look reverted; V4 is empty.

V4 gives the clearest and most correct picture.

Other tools, from their documentation:

- Gerrit diffs any two patch sets, marks "rebase edits" by inspecting the diff between parents, and skips files
  whose edits were all due to rebase. Its Reviewed flag is stored per revision.
- Reviewable tracks reviewed state per file, per revision, per reviewer, keeps old commits under
  `refs/reviewable`, and collapses changes between the old and new base.

## 5. The PR diff and its cost

GitHub shows a three-dot diff, and the fingerprint must use it: a two-dot diff contains the reverse of every base
commit, so marks would churn on every base move. Which merge-base GitHub picks when there are several is unverified.

Measured on git/git, `v2.44.0...v2.45.0` (504 files, 2.1 MB `-U0` patch), warm cache, Windows, 3 runs:

| step | time |
|---|---|
| `diff-tree` file list | 142–155 ms |
| `diff -U0` of the whole PR, one process | 597–700 ms |
| `diff -U0` plus all 504 fingerprints | 623–773 ms |
| `cat-file --batch` for all old and new blobs | 471–536 ms |
| replay of the whole PR with `merge-tree --merge-base` | 171–206 ms |
| 50 files, one `git diff` process each | 3.7 s |
| 50 files, `merge-file` + `rev-parse` processes each | 17.3 s |
| blobless clone of git/git | 24.9 s |
| blobless: first PR diff (lazy blob fetch) | 6.8 s |
| blobless: second PR diff | 0.66 s |

Rule: run whole-PR operations in one process each. A full keep/clear pass costs about 1 s warm. Whether
`merge-tree` batches its prefetch in a partial clone is unverified.

## 6. Hunk-level marks

No redesign needed. A hunk of `diff(M1,H1)` is unchanged since review iff its range in H1 does not intersect any
hunk of `diff(T,H1)`; on a conflict, fall back to hash membership. Hash-subset matching alone is not enough (D got
j1 and j2 wrong). `-U0` hunk boundaries merge or split when the author edits next to a hunk, so a hunk mark should
store the reviewed line range in H0 plus its hash, mapped through T (design only, not run).

## 7. Syncing with GitHub's Viewed state

`viewerViewedState` is VIEWED, UNVIEWED or DISMISSED ("the file has new changes since last viewed").
`markFileAsViewed` and `unmarkFileAsViewed` take only `pullRequestId` and `path`: no commit parameter, no
compare-and-set. What GitHub counts as a file change is unverified.

1. Local is the source of truth for Arc's display. Write to GitHub only when the PR head equals the reviewed head;
   check `headRefOid` before and after the call.
2. GitHub DISMISSED, local keep: local wins; re-mark on GitHub.
3. GitHub VIEWED, local clear: local wins; unmark on GitHub.
4. GitHub UNVIEWED after Arc last synced VIEWED: the person unticked it in the browser; GitHub wins.
5. GitHub VIEWED with no local mark: import it as a mark at the current head.
6. Both clear: nothing to reconcile.

## 8. Known failure cases of F

- Base-wide reformat or CRLF conversion (i3): false clear.
- A conflict and a position move in the same file: the hash fallback cannot see position, so a false keep.
- Old commits unavailable: F degrades to B and inherits its errors on j2 and k. Pin the reviewed heads.
- A single replay of the squashed PR can merge differently from the author's commit-by-commit rebase, giving a
  false clear (not observed; unverified).
- Not tested: submodules, LFS, empty files, `\ No newline` toggles.

## 9. Open risks

- GitHub's exact criterion for DISMISSED.
- `merge-tree` behaviour in a partial clone on the first look at a PR.
- Parsing `merge-tree -z --name-only` for rename/delete conflicts; the spike read only the tree line and exit code.
- The scenarios are synthetic 40-line files; how often b2-style conflicts happen in real PRs is unknown.
