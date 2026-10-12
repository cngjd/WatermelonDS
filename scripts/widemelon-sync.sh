#!/usr/bin/env bash
# Rebases the WatermelonDS Wide (WideMelon) branches onto the latest upstream WatermelonDS.
#
# Run from the root of a full-history checkout of the `widescreen` branch, with the melonDS-android-lib submodule
# initialised (both remotes must be fetchable). It never pushes: it only rewrites local branches and prints the result
# as KEY=VALUE lines (also appended to $GITHUB_OUTPUT when set).
#
#   changed=true|false     whether upstream had new commits
#   upstream=<sha>         upstream WatermelonDS commit now used as base
#   lib_head=<sha>         rebased lib commit (lib-widescreen)
#
# Exits non-zero on a rebase conflict, leaving nothing half-applied.
set -euo pipefail

UPSTREAM_APP_URL="${UPSTREAM_APP_URL:-https://github.com/SapphireRhodonite/WatermelonDS.git}"
UPSTREAM_APP_BRANCH="${UPSTREAM_APP_BRANCH:-Develop}"
UPSTREAM_LIB_URL="${UPSTREAM_LIB_URL:-https://github.com/SapphireRhodonite/melonDS-android-lib.git}"
APP_BRANCH="${APP_BRANCH:-widescreen}"
LIB_DIR="melonDS-android-lib"
BUMP_SUBJECT="Bump melonDS-android-lib to the WideMelon widescreen branch"

output() {
    echo "$1=$2"
    if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
        echo "$1=$2" >> "$GITHUB_OUTPUT"
    fi
}

git checkout -q "$APP_BRANCH"
git fetch -q --no-tags "$UPSTREAM_APP_URL" "$UPSTREAM_APP_BRANCH"
upstream=$(git rev-parse FETCH_HEAD)
output upstream "$upstream"

if git merge-base --is-ancestor "$upstream" HEAD; then
    output changed false
    output lib_head "$(git ls-tree HEAD "$LIB_DIR" | awk '{print $3}')"
    exit 0
fi
output changed true

old_base=$(git merge-base HEAD "$upstream")
old_lib_base=$(git ls-tree "$old_base" "$LIB_DIR" | awk '{print $3}')
new_lib_base=$(git ls-tree "$upstream" "$LIB_DIR" | awk '{print $3}')
current_lib=$(git ls-tree HEAD "$LIB_DIR" | awk '{print $3}')

echo "Upstream ${old_base:0:10} -> ${upstream:0:10}, lib ${old_lib_base:0:10} -> ${new_lib_base:0:10}"

# 1. Lib: replay our lib commits (old_lib_base..current_lib) onto the lib commit upstream now pins
(
    cd "$LIB_DIR"
    git fetch -q --no-tags "$UPSTREAM_LIB_URL" "$new_lib_base" 2>/dev/null \
        || git fetch -q --no-tags "$UPSTREAM_LIB_URL"
    git checkout -q --detach "$current_lib"
    if [[ "$old_lib_base" != "$new_lib_base" ]]; then
        if ! git rebase -q --onto "$new_lib_base" "$old_lib_base"; then
            git rebase --abort || true
            echo "::error::Conflict rebasing the melonDS-android-lib WideMelon commits onto ${new_lib_base}" >&2
            exit 1
        fi
    fi
    git branch -f lib-widescreen HEAD
)
lib_head=$(git -C "$LIB_DIR" rev-parse HEAD)
output lib_head "$lib_head"

# 2. App: replay our commits onto upstream, dropping the old submodule bump (re-created below)
if ! GIT_SEQUENCE_EDITOR="sed -i.bak -e '/ ${BUMP_SUBJECT}\$/d'" \
        git -c core.hooksPath=/dev/null rebase -q -i --onto "$upstream" "$old_base" "$APP_BRANCH"; then
    git rebase --abort || true
    echo "::error::Conflict rebasing the WatermelonDS Wide commits onto ${upstream}" >&2
    exit 1
fi

# 3. Point the submodule at the rebased lib commit
git update-index --cacheinfo "160000,${lib_head},${LIB_DIR}"
git -c core.hooksPath=/dev/null commit -q -m "$BUMP_SUBJECT" \
    -m "Rebased onto WatermelonDS ${upstream:0:10} (lib ${new_lib_base:0:10})."
git submodule update -q "$LIB_DIR"

echo "Rebased ${APP_BRANCH} onto ${upstream:0:10}"
