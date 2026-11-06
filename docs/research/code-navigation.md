# Code navigation: tree-sitter index and language servers

Run on Windows 11 x64 (Intel Core Ultra 7 268V, 32 GB), JDK Corretto 25.0.1, on 2026-11-06. Everything states
behaviour as measured or read on that date. **[unverified]** marks claims not checked. Language servers were not
run on any platform; that section is from their sources and docs. The macOS libraries were cross-compiled but
never loaded.

How to run, from the spike folder `arc-review-spikes/code-navigation` (kept outside this repo), with `JAVA_HOME`
set to a JDK 25:

```
cd prototype && ./gradlew installDist && cd ..
B=prototype/build/install/arc-code-navigation-spike/bin/arc-code-navigation-spike
$B smoke                                   # load and compile every grammar and query
$B index <repo@rev> <other-rev> [--threads N] [--highlight]
$B errors <repo@rev> [--as g1,g2 --ext sql]
$B outline <repo@rev> <path>
$B query <names> <repo@rev>...
$B navquality <n> <seed> <repo@rev>... [--kinds Call,Type] [--labels f]
```

`native/build-natives.sh` rebuilds the native libraries with zig; `native/copy-queries.sh` copies the grammars'
query files; `fixtures/make-fixtures.sh` turns the fixtures into git repos.

## Recommendation

1. **Binding.** `io.github.tree-sitter:jtreesitter` 0.26.1 (MIT, Java 23+, FFM). It ships no natives, so Arc
   builds its own: tree-sitter core v0.26.13 plus one shared library per grammar for windows-x64, macos-aarch64
   and macos-x64, all cross-compiled from Windows with `zig cc`. They load through jtreesitter's
   `NativeLibraryLookup` service interface, unpacked from the jar to a cache folder. Add `native/arc_shim.c`: one
   function, `arc_query_records(tree, query, out, capacity)`, that runs a query natively and returns flat
   `(match, capture, start, end)` records in a single call. jtreesitter alone creates a Java object per capture
   and spent about three times as long on extraction as on parsing. The shim reads jtreesitter's package-private
   native pointers by reflection; before production, upstream a bulk-captures API or generate Arc's own bindings
   with jextract, as `stretch-gpu-native` does.
2. **Grammars** (all MIT):

   | Language | Grammar | Notes |
   |---|---|---|
   | Java | tree-sitter/tree-sitter-java v0.23.5 | |
   | Kotlin, `.kts` | fwcd/tree-sitter-kotlin `main` @ d6af96d | Pin a commit; the last tag is from 2024 |
   | JavaScript, JSX | tree-sitter/tree-sitter-javascript | |
   | TypeScript, TSX | tree-sitter/tree-sitter-typescript | |
   | T-SQL | alfpricot/tree-sitter-mssql v1.0.0 @ 852cbd2 | One maintainer; v1.0.0 published 2026-11-04 |
   | JSON, YAML, Markdown | tree-sitter/tree-sitter-json, tree-sitter-grammars/tree-sitter-yaml and -markdown | |

   Rejected: DerekStride/tree-sitter-sql and Crary-Systems/tree-sitter-tsql (unusable on T-SQL);
   tree-sitter-grammars/tree-sitter-kotlin (idle since 2025-01, no context parameters).
3. **T-SQL fallback.** When more than about 20% of a file's characters fall under ERROR nodes, use Stretch's
   keyword tokenizer for that file. If the grammar stalls, fork it.
4. **Where the code lives.** The tree-sitter runtime, natives, grammars, queries, index and navigation belong in
   Arc. `stretch-code` is multiplatform with a wasmJs target and jtreesitter is JVM-only; highlighting and the
   index should share one parse per blob. Stretch gets only a generic way to pass tokens in.
5. **Navigation.** Name lookup, then a Kotlin/Java scope filter, then scoring. Without the filter, ambiguous
   clicks are mostly wrong.
6. **Language servers.** A trusted-only tier: Arc-pinned binaries in restricted modes, behind an explicit trust
   action.

## Binding comparison

| Option | Licence | Min JDK | Natives | Verdict |
|---|---|---|---|---|
| jtreesitter (tree-sitter/java-tree-sitter) 0.26.1 | MIT | 23 | None shipped; any plain C grammar library loads | Recommended. 0.26.0 crashed on Windows; 0.26.1 ran fine. |
| ktreesitter 0.25.1 | MIT | 17 | Core JNI bundled; grammars need generated JNI glue per OS | No. Open bugs shift byte offsets after non-BMP characters and break predicates on non-ASCII text. |
| tree-sitter-ng (bonede) 0.26.6 | MIT | [unverified] | Core and per-language jars, but stale (Kotlin from 2024) | Viable second choice. |
| WASM on the JVM (Chicory / Endive, treesitter4j) | Apache-2.0 | 17 | None needed | Young, no published benchmarks. Worth watching: it removes native packaging. |
| tree-sitter CLI as a subprocess | MIT | n/a | Binary per platform | No. Process per file and text-only output. |

Library sizes for the recommended set: about 23 MB on Windows, about 21 MB per macOS architecture. The Kotlin
(6.3 MB) and T-SQL (10.6 MB) parsers dominate. Building all Windows grammars takes about 3 min 20 s. On Windows
the shim must not use `__declspec(dllexport)`: one explicit export stops MinGW auto-exporting the `ts_*` symbols
and jtreesitter then fails to initialise.

## Grammar quality

Share of characters under ERROR nodes:

| Corpus | Grammar | Files | Files with errors | Chars in ERROR |
|---|---|---|---|---|
| stretch | kotlin (incl. `.kts`) | 446 | 0 | 0% |
| squash | kotlin | 560 | 0 | 0% |
| spring-framework | kotlin | 396 | 0 | 0% |
| spring-framework | java | 9,293 | 58 | 0.0% |
| excalidraw | typescript / tsx | 372 / 323 | 2 / 3 | 0.0% |
| all | json, yaml, markdown | about 300 | 0 | 0% |

A modern-Kotlin fixture (context parameters, guards in `when`, value classes, Compose lambdas, a `.kts` script)
parses clean. Java errors are all JSpecify varargs (`String @Nullable ... x`).

T-SQL, three grammars on the same files:

| Corpus | tree-sitter-mssql | DerekStride sql | Crary tsql |
|---|---|---|---|
| Fixture procedures (GO, CREATE OR ALTER, MERGE, CTEs, #temp, TVPs, TRY/CATCH, dynamic SQL, cursors, triggers) | 0.6% | 64% | 71% |
| Ola Hallengren maintenance solution (1.5 MB) | 0.1% | 90.5% | 72.5% |
| Brent Ozar First Responder Kit (3.3 MB) | 0.1% | 89.9% | 79.1% |
| microsoft/sql-server-samples (10.8 MB, mixed) | 1.0% | 77% | 57% |

What tree-sitter-mssql misses: `;WITH` after a statement that already ends in `;`, cursor variables, a trailing
comma in a column list, xml `.value()` calls, `RESTORE`, `ALTER DATABASE ... ADD FILEGROUP`. Errors stay local:
the outline of a 10,497-line script with 30 errors still lists every procedure and temp table.

## Measurements

- spring-framework, 9,832 files (55 MB): cold index 6.4–10.3 s on 8 threads, 26.8 s on one. Heap 205 MB, peak
  process memory 555 MB. 82,610 names, 110,197 definitions, 2,187,255 usages; name index builds in 280–520 ms and
  a lookup takes microseconds.
- Switching to a revision 20 or 39 commits back re-parses only the 27 or 52 changed blobs: about 140 ms, plus
  about 280 ms to rebuild the name index.
- excalidraw, 851 TS/TSX files: 1.4 s cold. stretch plus squash (1,373 files): about 1.4 s.
- Parse alone: about 5 MB/s per thread. Parse plus highlight on one thread: 2.1 M chars/s, about 50 ms for a
  100 KB file.
- `ls-tree` takes 130–210 ms per revision; reading blobs through one `git cat-file --batch` process is not the
  bottleneck.

tree-sitter's edit-and-reparse is not what makes revision switching cheap; reuse by blob id is.

## Cross-repo and cross-language

Indexing a T-SQL fixture repo and a Java/Kotlin fixture repo together resolves `{call sales.usp_PlaceOrder(?, ?)}`
in Java and Kotlin strings to the `CREATE OR ALTER PROCEDURE`, `SELECT sales.ufn_CustomerBalance(?)` to its
function, and `FROM sales.Customer` (including in a Kotlin `"""` string) to `CREATE TABLE [sales].[Customer]`.
SQL names are taken from a string literal only when it reads as SQL: the name after EXEC, call, FROM, JOIN, INTO,
UPDATE or MERGE, or a qualified name followed by `(`. Matching is case-insensitive with brackets stripped; a
schema match is a ranking bonus.

## Navigation quality

600 random call and type references from stretch plus squash, answered three ways:

- **name match**: every definition with that name in the same language family;
- **scores**: same file +10, imported +8, same package +6, wildcard import +4, same repo +2, kind match +3 or
  mismatch −6, arity match +3 / fits +1 / mismatch −3, main over test +1, SQL schema +4;
- **scope + scores**: for Kotlin and Java, a filter first. A call needs a callable or a type; a local definition
  is visible only in its own file; main code cannot see test paths; an unqualified call cannot reach a member of
  another file's class; a name from another package must be imported or be in the same package.

Hand-labelled clicks that had two or more name matches:

| | name match | scores | scope + scores |
|---|---|---|---|
| Design set, 40 clicks | 9 acceptable | 22 right | 36 right, 1 missed, 3 wrong |
| Held-out set, 30 clicks | 7 acceptable | 20 right | 27 right, 2 missed, 1 wrong |

About 45% of those clicks are library symbols with no definition in the sources, where the right answer is "none".
Remaining misses: a member called unqualified inside a receiver lambda, and a qualified call whose receiver type
is inferred. Show filtered-out candidates as a dimmed secondary list instead of nothing. Argument count and
whether the name is qualified are computed at click time from the open file's tree.

TypeScript (231 calls, scores only): exactly one answer 47%, a short list 10%, noise 0.4%. An import-based rule
using `tsconfig` paths, read from git objects, would do better; it was not built.

The grammars' own `tags.scm` files miss Java enums, records, fields and constructors and TypeScript type aliases
and enums. Arc must own its definition queries.

## Highlighting in Stretch

`stretch-code` has six `TokenKind`s (Keyword, String, Number, Comment, Type, Function). `CodeViewer` and
`DiffViewer` tokenize internally, the diff per hunk from patch lines, and accept no tokens from outside.
Tree-sitter spans convert exactly to `CodeToken(start, end, kind)`.

Stretch changes needed: a `tokens` / `CodeHighlighter` parameter on `CodeViewer` defaulting to the current
tokenizer, and whole-file old and new tokens per file on `DiffViewer`. Capture names map to kinds by their first
dotted part: keyword-like to Keyword; string, character, escape to String; number, float, boolean,
constant.builtin to Number; comment to Comment; type, constructor, attribute to Type; function, method to
Function; variable, property, parameter, punctuation, operator stay plain. The narrower capture wins; for the same
node the later pattern wins; name-like kinds paint leaf nodes only.

## Language servers

| Server | Licence | Runs repo code by default | Restricted mode |
|---|---|---|---|
| Eclipse JDT LS | EPL-2.0 (separate process) | Yes: Gradle import evaluates build scripts and uses the wrapper's `distributionUrl`; Maven runs mojos; the Eclipse `.project` importer runs even with both off | Only its syntax server |
| JetBrains kotlin-lsp (alpha) | Repo Apache-2.0, partly closed-source; binary licence [unverified] | Yes: Gradle import with an init script | Import disabled, or an app-generated JSON workspace |
| typescript-language-server | Apache-2.0 | Yes: prefers the workspace's own TypeScript and loads tsconfig plugins | Pin Arc's `tsserver`, disable type acquisition and plugins |
| TypeScript 7 native, `tsc --lsp` | Apache-2.0 | No plugin support | Pin Arc's binary. Safest TypeScript option. |
| microsoft/sqltoolsservice | MIT | Low for `.sql` files | Offline syntax diagnostics; go-to-definition needs a connection |

No server is safe on an untrusted branch in its default mode. Without build import, JDT LS and kotlin-lsp lose
dependency resolution, so on untrusted code they add less over the tree-sitter tier than the design assumed.

LSP client: LSP4J 1.0.0 is `EPL-2.0 OR BSD-3-Clause` per its LICENSE and NOTICE, so it is usable under BSD-3; its
Maven POMs list only EPL-2.0, so licence scanners will flag it.

## Risks

- tree-sitter-mssql has one maintainer and is new; pin it and keep a fork-ready copy.
- fwcd Kotlin has no tags since 2024 and its highlights query lags its grammar.
- Arc owns a native build for three targets, plus macOS signing and notarisation of the libraries [unverified].
- The shim's reflection into jtreesitter may break on upgrade.
- Memory: the prototype stores one object per usage; packed arrays would cut it several times.
- Indexing needs every blob at a revision, so a blobless clone needs the batched prefetch described in
  `github-api.md`.
- Kotlin quality was judged on stretch and squash; the T-SQL fixtures are synthetic.

## Design changes recommended

1. Tier one is tree-sitter plus a scope-aware resolver, not a plain name index.
2. Index facts by blob id, independent of path and revision.
3. Store definitions with parameter count and container kind, and usages as compact records.
4. Arc owns its queries; the grammars' are starting points.
5. Link SQL across languages from string literals that read as SQL.
6. Stretch accepts tokens from outside in `CodeViewer` and `DiffViewer`.
7. Language servers are trust-gated, pinned and restricted.
8. Batch-prefetch blobs for revisions being indexed.
