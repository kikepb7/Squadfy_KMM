#!/usr/bin/env bash
# Checks that Android (gradle/libs.versions.toml) and iOS (iosApp/Configuration/Config.xcconfig) carry the
# expected SemVer version (ADR-0009). Usage: scripts/check-version.sh X.Y.Z
set -euo pipefail
cd "$(dirname "$0")/.."

expected="${1:?Usage: $0 X.Y.Z}"
android=$(sed -nE 's/^projectVersionName = "([^"]+)"/\1/p' gradle/libs.versions.toml)
ios=$(sed -nE 's/^MARKETING_VERSION=(.+)$/\1/p' iosApp/Configuration/Config.xcconfig)

status=0
[ "$android" = "$expected" ] || { echo "::error::Android projectVersionName is '$android', expected '$expected'" >&2; status=1; }
[ "$ios" = "$expected" ] || { echo "::error::iOS MARKETING_VERSION is '$ios', expected '$expected'" >&2; status=1; }
[ "$status" -eq 0 ] && echo "✅ Version $expected on Android and iOS"
exit "$status"
