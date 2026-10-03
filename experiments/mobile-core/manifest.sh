#!/usr/bin/env bash
set -euo pipefail

if [[ "${FERRY_DEVCONTAINER:-}" != "1" ]]; then
  echo 'FERRY_DEVCONTAINER=1 is required; refusing host-toolchain execution' >&2
  exit 2
fi
source_sha="${FERRY_SOURCE_SHA:-}"
if [[ ! "$source_sha" =~ ^[0-9a-f]{40}$ ]]; then
  echo 'FERRY_SOURCE_SHA must be a full Git commit SHA' >&2
  exit 2
fi

aar_path="${1:-dist/ferry-mobile-core.aar}"
if [[ ! -f "$aar_path" ]]; then
  echo "missing AAR: $aar_path" >&2
  exit 1
fi

printf '%s\n' 'FERRY_BRIDGE_MANIFEST'
printf 'source_sha=%s\n' "$source_sha"
printf 'module=%s\n' "$(awk '$1 == "module" {print $2}' go.mod)"
printf 'gomobile=%s\n' "${GOMOBILE_VERSION:-v0.0.0-20260908204917-8b95e45f8d3e}"
printf 'gobind=%s\n' "${GOBIND_VERSION:-v0.0.0-20260908204917-8b95e45f8d3e}"
printf 'gradle_wrapper_version=%s\n' "$(sed -n 's#^distributionUrl=.*gradle-\([^/]*\)-bin.zip#\1#p' gradle/wrapper/gradle-wrapper.properties)"
printf 'gradle_wrapper_distribution_url=%s\n' "$(sed -n 's/^distributionUrl=//p' gradle/wrapper/gradle-wrapper.properties | sed 's#\\:#:#')"
printf 'gradle_wrapper_distribution_sha256=%s\n' "$(sed -n 's/^distributionSha256Sum=//p' gradle/wrapper/gradle-wrapper.properties)"
printf 'aar=%s\n' "$aar_path"
printf 'aar_sha256=%s\n' "$(sha256sum "$aar_path" | awk '{print $1}')"
printf 'abi=arm64-v8a\n'
printf '%s\n' 'FERRY_BRIDGE_MANIFEST_END'
