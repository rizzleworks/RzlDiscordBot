#!/usr/bin/env bash
# Preview the release notes GitHub would generate for main since the last tag, without creating anything.
# Usage: scripts/preview-release-notes.sh [tag]
set -euo pipefail
cd "$(dirname "$0")/.."

# The tag only names the end of the "Full Changelog" link; the range is always last tag to main.
tag="${1:-main}"
previous="$(git describe --tags --abbrev=0 origin/main)"

gh api -X POST "repos/{owner}/{repo}/releases/generate-notes" \
    -f tag_name="$tag" \
    -f target_commitish=main \
    -f previous_tag_name="$previous" \
    --jq .body
