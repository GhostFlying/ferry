GO_MODULE := github.com/GhostFlying/ferry/experiments/mobile-core
GOMOBILE_VERSION := v0.0.0-20260908204917-8b95e45f8d3e
GRADLE_VERSION := 8.10.2

.PHONY: test aar manifest

test:
	go test ./...

aar:
	go install golang.org/x/mobile/cmd/gomobile@$(GOMOBILE_VERSION)
	go install golang.org/x/mobile/cmd/gobind@$(GOMOBILE_VERSION)
	mkdir -p dist
	gomobile bind -target=android -androidapi=29 -o dist/ferry-mobile-core.aar .

manifest:
	./manifest.sh dist/ferry-mobile-core.aar
