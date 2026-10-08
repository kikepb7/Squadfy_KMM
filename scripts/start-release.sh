#!/usr/bin/env bash
# Starts a release or hotfix branch (ADR-0009) and bumps the version on Android and iOS. It never pushes.
#   scripts/start-release.sh release 1.1.0   → release/1.1.0 from develop
#   scripts/start-release.sh hotfix 1.0.1    → hotfix/1.0.1 from main
set -euo pipefail
cd "$(dirname "$0")/.."

kind="${1:-}"
version="${2:-}"
case "$kind" in
  release) base=develop ;;
  hotfix) base=main ;;
  *) echo "Usage: $0 <release|hotfix> X.Y.Z" >&2; exit 1 ;;
esac
[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "The version must be SemVer X.Y.Z (got '$version')" >&2; exit 1; }
branch="$kind/$version"

[ -z "$(git status --porcelain)" ] || { echo "The working tree has uncommitted changes" >&2; exit 1; }
git fetch origin --quiet
git rev-parse --verify --quiet "refs/heads/$branch" >/dev/null && { echo "$branch already exists" >&2; exit 1; }
git rev-parse --verify --quiet "refs/tags/v$version" >/dev/null && { echo "Tag v$version already exists" >&2; exit 1; }

git switch --quiet "$base"
if git rev-parse --verify --quiet "refs/remotes/origin/$base" >/dev/null; then
  git merge --ff-only --quiet "origin/$base" || { echo "$base has diverged from origin/$base: sync it first" >&2; exit 1; }
fi
git switch --quiet -c "$branch"

perl -pi -e "s/^projectVersionName = \".*\"/projectVersionName = \"$version\"/" gradle/libs.versions.toml
perl -pi -e "s/^MARKETING_VERSION=.*/MARKETING_VERSION=$version/" iosApp/Configuration/Config.xcconfig
./scripts/check-version.sh "$version"

if [ -n "$(git status --porcelain)" ]; then
  git commit --quiet -am "RELEASE | $version · Bump version to $version"
fi

target=$([ "$kind" = release ] && echo "fixes go in fix/* branches with PRs into $branch" || echo "commit the fix here")
cat <<MSG
✅ $branch created from $base with version $version ($target).
Next:
  git push -u origin $branch
  PR $branch → main, merge, then tag:  git tag v$version origin/main && git push origin v$version
  PR $branch → develop (back-merge)
MSG
