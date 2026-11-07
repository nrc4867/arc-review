# Arc

A human-first code review app for Windows and macOS: one person reviews commits and pull requests across several repos and branches at once. Kotlin on the JVM, Compose Multiplatform for Desktop, built on Stretch, which is included as a composite build from source. The app's name is Arc; the binary and the repo are `arc-review`.

## Read before working

| What | Where |
| --- | --- |
| The design: model, features, data and hosting, build order, decisions | [Code review tool: design](https://claude.ai/artifact/Bq6CtuCH27F79TC1CLLhtQ) |
| The boards, the specification for every screen | [Arc design boards](https://claude.ai/artifact/3xbBdkCrd2zhjZw7bYSGVs) |
| Stretch: components and how apps use them | `../stretch/docs/using-stretch.md`, `../stretch/docs/development.md` |

## Build, run, test

Gradle needs JDK 25 as `JAVA_HOME` (`~/.jdks/corretto-25.0.1` on this machine); Gradle provisions the JetBrains Runtime the app runs on. Stretch is the sibling folder `../stretch`, or the checkout named by `stretch.dir` in `~/.gradle/gradle.properties`.

```
./gradlew build                      # compile, test and check every module
./gradlew :core:test                 # one module's tests; run only the module being changed while working
./gradlew :app:run                   # the app
./gradlew :app:run -Pdevtools=47811  # the app, with window screenshots at curl 127.0.0.1:47811/capture
./gradlew :app:run -Pdemo            # the app with the dev-only sample threads and staged comments
./gradlew :design:generateIcons      # ArcIcons.kt from design/icons/*.svg
```

Stop a running app before rebuilding; a rebuild under a running app breaks it. To see the running app, use Stretch's screenshot endpoint as described in `../stretch/.claude/skills/app-screenshot/SKILL.md`. Keyboard and mouse input cannot be sent to the window from a session; check input in a UI test (`runComposeUiTest`, `performKeyInput`).

The app's data folder is `%LOCALAPPDATA%\dev.chieppa.arc` on Windows and `~/Library/Application Support/dev.chieppa.arc` on macOS. It holds `settings.properties` (theme, size, whether panes wrap long lines, whether changes are highlighted in the gutter only, last open review), `repos.json` (each repo's name and the folder of its clone, shared by every review) and `reviews/<id>.json` (a review's sources, panes, marks, each source's view and each pane's state, its wrapping among it). The window opens on a review whatever state that folder is in, so a screen can be set up by writing those files before `:app:run`.

CI (`.github/workflows/ci.yml`) checks Stretch out beside the app and runs `build` on Windows and macOS. Linux builds too but is unsupported, so it cannot fail the run.

## Modules

Each module is a Gradle subproject in package `dev.chieppa.arc.<module>`. A module declares only the modules below it; Gradle enforces the direction.

| Layer | Module | Holds |
| --- | --- | --- |
| App | `app` | `main`, the window, the macOS menu bar, `AppSession` (the data folder: settings, reviews, repos) and saving the open review as it changes |
| Interface | `ui` | The main window on Stretch's frame. `workbench` (the window's state and actions), `review` (`ReviewState` and the source, pane and mark controllers), `services` (`ReviewData`, what the window reads, and its git implementation), `tree`, `files`, `sources`, `panes`, `find`, `finder`, `status`, `conversation` (threads and staged comments), `actions`, `menus`, `drawers`, `frame`, `settings` |
| Presentation | `design` | Arc's icons (generated from `design/icons`), Arc's colors beside Stretch's tokens, every user-facing string |
| Data | `host` | The `Host` interface and GitHub: reading a pasted link as a source. `ReviewConversation`, the threads and staged comments the store and the host will provide |
| Data | `git` | The git layer through the installed git CLI: one process per whole-source operation, one long-running `cat-file --batch` per repo, never a lazy blob fetch (a partial clone gets its trees in one batch). Sources, fingerprints, the mark replay. Repos are found through `RepoLocator` |
| Data | `store` | Local state as files: settings, the repo registry and one JSON file per review (kotlinx.serialization). Plain JVM, no Stretch |
| Core | `core` | The review model as plain immutable data: `Review`, `Source` and its ref syntax, `Pane`, changes, file marks with their fingerprint and `MarkRules`, progress, the conversation model. No dependencies |
| Dev only | `demo` | A sample `ReviewConversation`, on the app's classpath only with `-Pdemo` |

Everything below `ui` is plain Kotlin, unit tested without a window. In `ui`, every action is registered once and runs from the palette, the menus, its key and the context menus alike; a context menu item runs it on the menu's target through `Workbench.withTarget`, and `ActionTargets` says what an action acts on. A test lives with the module it tests. Git tests build small repos in temp folders through the git CLI; UI tests read from `FakeReviewData`.

Marks follow `docs/research/review-marks.md`: a mark holds the file's fingerprint (candidate B) and the reviewed head and merge-base; `MarkRules` keeps or clears it by the replay (candidate F) that `MarkReplayer` runs when a source has moved, falling back to the fingerprint where the replay conflicts or the reviewed commits are gone.

## Rules

**Build to the boards.** The boards are the specification for every screen: layout, drawers, rails, chips, status bar text, keyboard hints and icons are built as drawn. When a board needs a component Stretch lacks, look in Stretch first; if Stretch nearly has it and the need is general, the change goes in the Stretch repo, never patched from here. Only a need that is Arc's alone becomes an Arc component, drawn in Stretch's tokens.

**Arc's icons for Arc's functions.** Every icon for an Arc function is Arc's own, in `design/icons`, drawn on Stretch's 16px grid (1.5px round strokes, `currentColor`, small filled dots only). Where a board draws a Stretch icon, such as Files' folder, that icon is used. The frame's own controls keep Stretch's icons. Generated `ArcIcons.kt` is committed; `check` fails when it is stale.

**Clean panels.** Icons, labels, numbers and tooltips. Incomplete or conflicting state is shown in place by highlighting it, never with explanatory messages or banners. An empty drawer is plain.

**Text lives outside code.** Every user-facing string is a key in `design`'s `strings.xml`. An action's text key and icon key are its id.

**One per file, no magic values.** One composable or one service per file, files under about 300 lines, helpers in their own files, constants in named objects.

**Nothing leaves without a deliberate push.** Notes, marks and drafts stay local until a person publishes them in a confirmed step. An agent never marks, stages, pushes or approves.

**Comments and docs describe the code as it stands.** Current behaviour only, with no account of how it came to be. Public APIs get a one-line KDoc where the name is not enough.

**Licences.** Arc is AGPL-3.0-only, like Stretch. Any dependency must be permissive (Apache-2.0, MIT, BSD). Add a dependency only when code needs it.

## Git

- Commit to `main` in small logical commits and push to origin; no feature branches or pull requests unless asked.
- Commit messages describe the change, not the session. End with `Co-Authored-By: Claude <model> <noreply@anthropic.com>`.
- A change Stretch needs is made in the Stretch repo first; Arc then builds against it through the composite build.
