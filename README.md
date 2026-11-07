# Arc

Arc is a desktop app for reviewing code, for Windows and macOS, built on [Stretch](https://github.com/nrc4867/stretch).
One person reviews commits and pull requests across several repos and branches at once, in one saved workspace that
spans repos, where any file at any revision can sit next to any other.

The repo and the binary are named `arc-review`.

## Principles

- **Reading comes first.** The tool is judged by how fast and how comfortably someone can read and compare code.
- **Nothing leaves without a deliberate push.** Notes, marks and comment drafts are local until published.
- **Switching is instant and loses nothing.** Each commit, PR, branch and repo keeps its own view state.
- **AI is a guest.** The app has no model. An outside agent can read a review and leave marked suggestions, or be handed
  a checked-out source to fix; a person reviews what it did.

## Status

Reading local repos. There is one workspace, saved between runs: the strip under the top bar lists every source open
in it. Add a repo by its folder and open sources in it by ref (`@sha`, `@sha..sha`, `:branch...base`); opening a
pull request, commit, range or branch from a filter tab, Go to anything, a pasted link or "+" puts it in the strip
and switches to it, and each chip's close button takes it out (its marks stay, so opening it again restores them).
Each source keeps its own panes, scroll and open folders. The Files drawer
groups every source's changed files with their status, line counts and reviewed marks, a folder or selection marks at
once, and a source's tree can be scoped to one commit. Right-clicking a file, folder, source or tab offers what can be
done to it, and files drag from the tree onto a pane, an edge or out into a window of their own. Panes show a file's
diff (split or unified), the whole file with its changes marked, or rendered Markdown, with find in file and wrapping
of long lines; an added, deleted or unchanged file shows whole under a chip saying so. Pictures (PNG, JPEG, GIF, WebP,
BMP, ICO) show drawn, a changed one before and after side by side, and an SVG can show drawn or as its text. A setting
highlights changed lines whole or in the gutter only, leaving the code plain. Any file at any revision of any
repo can be opened and pinned beside the rest. Marks survive a rebase that keeps the change. The keyboard covers next
and previous file and change, mark and advance, find and wrapping, all in the command palette (Ctrl Shift P). Go to
anything (Ctrl K, or Ctrl P for files) searches the strip's sources, the files they change and the active source's
other files, and the open pull requests (by title, on GitHub) and local branches of every registered repo that the
strip does not hold: Enter switches, Ctrl Enter opens in a new pane, Alt Enter opens without switching. A pasted
github.com link or a ref shows what it points at, with the pull request a commit sits in, and opens it or its whole
pull request.
The workspace, repos and layouts are saved locally. Pull requests are read from GitHub with the `gh` login (read-only): a
pull request (`#412`) is opened as a source whose head is fetched from the clone's origin, and saved GitHub searches,
polled every five minutes in the Pull requests drawer, open as tabs listing each pull request's labels, who it waits on,
checks and comments, the title keeping its room as columns give way in a narrow pane, and an "Open" chip on those in
the strip. Clicking a pull request opens it, cloning its repo (blobless, into the app's data folder) when there is no
clone of it here; a right-click also opens it without switching, opens it on GitHub, and copies its link, number,
reference, branch, head commit or title. A merged or closed pull request shows dimmed on its chip and its tree row,
and one action closes them all. Rules raise desktop notifications for
comments on the person's pull requests, mentions, and new pull requests in a filter or with a label; the bell counts
them. Every head a source is read at is recorded and kept under a private ref, and a pull request also takes the heads
GitHub knows of (force pushes and reviewed commits); the revision matrix sets the source's files against those
revisions, showing which file changed in which, compared through rebases, where each was reviewed and where it changed
after, with arrow keys to move and Enter to open a file at a revision, and the Commits drawer picks any two commits to
open the files whose change differs between them. Threads and staged comments are drawn only from the dev-only sample
(`-Pdemo`).

## Build and run

Needs JDK 25 as `JAVA_HOME` and a checkout of [Stretch](https://github.com/nrc4867/stretch) beside this one (or named
by `stretch.dir` in `~/.gradle/gradle.properties`).

```
./gradlew build        # compile and test
./gradlew :app:run     # run the app
./gradlew :app:run -Pdemo   # run it with sample threads and staged comments
```

## Links

- Design: https://claude.ai/artifact/Bq6CtuCH27F79TC1CLLhtQ
- Design boards: https://claude.ai/artifact/3xbBdkCrd2zhjZw7bYSGVs

## Licence

AGPL-3.0-only. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
