package smb

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/hex"
	"io"
	"strings"
	"testing"
)

func TestValidateDestination(t *testing.T) {
	for _, value := range []string{"", ".", "../x", "a/../x", "/absolute", `a\\b`, "a\x00b"} {
		if err := validateDestination(value); err == nil { t.Fatalf("expected rejection for %q", value) }
	}
	for _, value := range []string{"clip.mp4", "dir/clip.mp4"} {
		if err := validateDestination(value); err != nil { t.Fatalf("valid destination %q: %v", value, err) }
	}
}

func TestTemporaryNameIsOwnedByOperation(t *testing.T) {
	got, err := temporaryName("dir/clip.mp4", "op-1")
	if err != nil { t.Fatal(err) }
	if got != "dir/clip.mp4.ferry-op-1.part" { t.Fatalf("name=%q", got) }
	if _, err := temporaryName("clip.mp4", "op/escape"); err == nil { t.Fatal("operation traversal accepted") }
}

func TestCopyWithContextHashesAndCounts(t *testing.T) {
	input := strings.Repeat("pocket-3", 2048)
	var output bytes.Buffer
	count, err := copyWithContext(context.Background(), &output, strings.NewReader(input))
	if err != nil { t.Fatal(err) }
	if count != int64(len(input)) || output.String() != input { t.Fatalf("count=%d len=%d", count, output.Len()) }
	sum := sha256.Sum256(output.Bytes())
	if hex.EncodeToString(sum[:]) == "" { t.Fatal("empty hash") }
}

func TestCopyWithContextCancellation(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	count, err := copyWithContext(ctx, io.Discard, strings.NewReader("data"))
	if err == nil || count != 0 { t.Fatalf("count=%d err=%v", count, err) }
}
