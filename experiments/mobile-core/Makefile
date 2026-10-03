GO_MODULE := github.com/GhostFlying/ferry/experiments/mobile-core
GOMOBILE_VERSION := v0.0.0-20260908204917-8b95e45f8d3e
GOBIND_VERSION := v0.0.0-20260908204917-8b95e45f8d3e
GRADLE_VERSION := 8.10.2
GOBIN := $(shell go env GOPATH)/bin
export PATH := $(GOBIN):$(PATH)

.PHONY: test aar manifest

define require_container
	@test "$${FERRY_DEVCONTAINER:-}" = "1" || { echo 'FERRY_DEVCONTAINER=1 is required; refusing host-toolchain execution' >&2; exit 2; }
endef

test:
	$(call require_container)
	go test ./...

aar:
	$(call require_container)
	./gradlew --version
	go install golang.org/x/mobile/cmd/gomobile@$(GOMOBILE_VERSION)
	go install golang.org/x/mobile/cmd/gobind@$(GOMOBILE_VERSION)
	mkdir -p dist
	$(GOBIN)/gomobile init
	$(GOBIN)/gomobile bind -target=android -androidapi=29 -o dist/ferry-mobile-core.aar .

manifest:
	$(call require_container)
	GOMOBILE_VERSION=$(GOMOBILE_VERSION) GOBIND_VERSION=$(GOBIND_VERSION) ./manifest.sh dist/ferry-mobile-core.aar
