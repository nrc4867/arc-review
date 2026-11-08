#!/usr/bin/env bash
# Builds the tree-sitter core (with arc_shim.c) and every grammar in grammars.tsv as shared libraries for Windows x64,
# macOS arm64 and macOS x64, from any host, with zig cc. Sources are fetched at their pinned commits into
# build/native-src unless NATIVE_SRC names a folder already holding them. Output goes to the module's resources.
# Usage: native/build-natives.sh [platform...]   (ZIG names the zig executable; default: zig on the PATH)
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
module="$(dirname "$here")"
zig="${ZIG:-zig}"
src_root="${NATIVE_SRC:-$module/build/native-src}"
out_root="$module/src/main/resources/dev/chieppa/arc/syntax/natives"

declare -A target=([windows-x64]=x86_64-windows-gnu [macos-aarch64]=aarch64-macos [macos-x64]=x86_64-macos)
declare -A prefix=([windows-x64]="" [macos-aarch64]=lib [macos-x64]=lib)
declare -A suffix=([windows-x64]=.dll [macos-aarch64]=.dylib [macos-x64]=.dylib)

# Fetches repository $2 at commit $3 into $1, or checks that $1 already holds that commit.
checkout() {
  local dir="$1" repo="$2" commit="$3"
  if [ -d "$dir/.git" ]; then
    local head; head="$(git -C "$dir" rev-parse HEAD)"
    [ "$head" = "$commit" ] || { echo "$dir is at $head, not the pinned $commit" >&2; exit 1; }
    return
  fi
  mkdir -p "$dir"
  git -C "$dir" init -q
  git -C "$dir" fetch -q --depth 1 "$repo" "$commit"
  git -C "$dir" -c advice.detachedHead=false checkout -q FETCH_HEAD
}

names=(); folders=()
while IFS=$'\t' read -r name repo commit folder; do
  case "$name" in ''|'#'*) continue ;; esac
  dir="$src_root/$(basename "$repo" .git)"
  checkout "$dir" "$repo" "$commit"
  names+=("$name"); folders+=("$dir/$folder")
done < "$here/grammars.tsv"

platforms=("$@")
[ ${#platforms[@]} -eq 0 ] && platforms=(windows-x64 macos-aarch64 macos-x64)

for p in "${platforms[@]}"; do
  out="$out_root/$p"
  rm -rf "$out"; mkdir -p "$out"
  t="${target[$p]}"
  lib() { echo "$out/${prefix[$p]}$1${suffix[$p]}"; }
  for i in "${!names[@]}"; do
    name="${names[$i]}"; src="${folders[$i]}"
    echo "[$p] $name"
    if [ "$name" = core ]; then
      "$zig" cc -target "$t" -shared -O2 -fPIC -std=c11 -s -I "$src/include" -I "$src/src" \
        "$src/src/lib.c" "$here/arc_shim.c" -o "$(lib tree-sitter)" &
    else
      files=("$src/parser.c")
      [ -f "$src/scanner.c" ] && files+=("$src/scanner.c")
      "$zig" cc -target "$t" -shared -O2 -fPIC -std=c11 -s -I "$src" "${files[@]}" -o "$(lib "tree-sitter-$name")" &
    fi
  done
  wait
  # zig leaves import libraries and debug files beside Windows libraries; only the libraries ship.
  rm -f "$out"/*.lib "$out"/*.pdb
done
ls -la "$out_root"/*
