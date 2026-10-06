#!/usr/bin/env bash
set -euo pipefail

: "${FERRY_DEVCONTAINER:?run bridge builds in the Ferry devcontainer}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$ROOT_DIR/android/app/libs"
mkdir -p "$OUT_DIR"

if ! command -v gomobile >/dev/null 2>&1; then
  GOBIN="$(go env GOPATH)/bin" go install golang.org/x/mobile/cmd/gomobile@v0.0.0-20260908204917-8b95e45f8d3e
fi

export PATH="$(go env GOPATH)/bin:$PATH"
gomobile init
go test ./...
gomobile bind -target=android -androidapi 29 -o "$OUT_DIR/ferry-bridge.aar" ./go/mobile/bridge
