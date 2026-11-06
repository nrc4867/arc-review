# Arc

Arc is a desktop app for reviewing code, for Windows and macOS, built on [Stretch](https://github.com/nrc4867/stretch).
One person reviews commits and pull requests across several repos and branches at once: a review is a saved workspace
that can span repos, and any file at any revision can sit next to any other.

The repo and the binary are named `arc-review`.

## Principles

- **Reading comes first.** The tool is judged by how fast and how comfortably someone can read and compare code.
- **Nothing leaves without a deliberate push.** Notes, marks and comment drafts are local until published.
- **Switching is instant and loses nothing.** Each commit, PR, branch and repo keeps its own view state.
- **AI is a guest.** The app has no model. An outside agent can read a review and leave marked suggestions, or be handed
  a checked-out source to fix; a person reviews what it did.

## Status

Design stage. No code yet.

- Design: https://claude.ai/artifact/Bq6CtuCH27F79TC1CLLhtQ
- Design boards: https://claude.ai/artifact/3xbBdkCrd2zhjZw7bYSGVs

## Licence

AGPL-3.0-only. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
