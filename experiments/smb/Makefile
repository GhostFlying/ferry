.PHONY: test

define require_container
	@test "$${FERRY_DEVCONTAINER:-}" = "1" || { echo 'FERRY_DEVCONTAINER=1 is required; refusing host-toolchain execution' >&2; exit 2; }
endef

test:
	$(call require_container)
	go test ./...
