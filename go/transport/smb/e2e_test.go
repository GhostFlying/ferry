package smb

import (
	"bytes"
	"context"
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"fmt"
	"io"
	"net"
	"os"
	"testing"
	"time"
)

// These tests are opt-in so ordinary unit tests do not need a network service
// or credentials. The service must be a real SMB server prepared for the run.
func e2eClient(t *testing.T) *Client {
	t.Helper()
	if os.Getenv("FERRY_SMB_E2E") != "1" {
		t.Skip("set FERRY_SMB_E2E=1 to run against a controlled SMB server")
	}
	env := func(name string) string {
		value := os.Getenv(name)
		if value == "" {
			t.Fatalf("%s is required", name)
		}
		return value
	}
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()
	conn, err := (&net.Dialer{}).DialContext(ctx, "tcp", env("FERRY_SMB_TARGET"))
	if err != nil {
		t.Fatalf("dial: %v", err)
	}
	client, err := NewClient(ctx, conn, env("FERRY_SMB_USER"), env("FERRY_SMB_PASSWORD"), env("FERRY_SMB_SHARE"))
	if err != nil {
		_ = conn.Close()
		t.Fatalf("connect: %v", err)
	}
	t.Cleanup(func() { _ = client.Close() })
	return client
}

func e2eName() string {
	return fmt.Sprintf("ferry-e2e-%d.bin", time.Now().UnixNano())
}

func assertAbsent(t *testing.T, client *Client, names ...string) {
	t.Helper()
	for _, name := range names {
		if _, err := client.share.Stat(name); !errors.Is(err, os.ErrNotExist) {
			t.Errorf("%s should not exist after cancellation, stat err=%v", name, err)
		}
	}
}

func randomPayload(t *testing.T, size int) ([]byte, string) {
	t.Helper()
	data := make([]byte, size)
	if _, err := rand.Read(data); err != nil {
		t.Fatal(err)
	}
	digest := sha256.Sum256(data)
	return data, hex.EncodeToString(digest[:])
}

type cancelAfterReader struct {
	source io.Reader
	limit  int
	read   int
	cancel context.CancelFunc
}

func (r *cancelAfterReader) Read(buf []byte) (int, error) {
	n, err := r.source.Read(buf)
	r.read += n
	if r.read >= r.limit {
		r.cancel()
	}
	return n, err
}

func TestE2ECancelDuringTemporaryWriteRemovesTemporary(t *testing.T) {
	client := e2eClient(t)
	destination := e2eName()
	data, digest := randomPayload(t, 8<<20)
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	_, err := client.Upload(ctx, "cancel-temp", destination, digest,
		&cancelAfterReader{source: bytes.NewReader(data), limit: 1 << 20, cancel: cancel})

	var uploadErr *Error
	if !errors.As(err, &uploadErr) || uploadErr.Stage != StageCancelled {
		t.Fatalf("expected cancelled stage, got %v", err)
	}
	temp, _ := temporaryName(destination, "cancel-temp")
	assertAbsent(t, client, destination, temp)
}

func TestE2ECancelDuringFinalCopyRemovesOwnedObjects(t *testing.T) {
	client := e2eClient(t)
	destination := e2eName()
	data, digest := randomPayload(t, 64<<20)
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	go func() {
		watcher := client.share.WithContext(context.Background())
		for ctx.Err() == nil {
			if _, err := watcher.Stat(destination); err == nil {
				cancel()
				return
			}
			time.Sleep(time.Millisecond)
		}
	}()

	_, err := client.Upload(ctx, "cancel-final", destination, digest, bytes.NewReader(data))

	var uploadErr *Error
	if !errors.As(err, &uploadErr) || uploadErr.Stage != StageCancelled {
		t.Fatalf("expected cancelled stage, got %v", err)
	}
	temp, _ := temporaryName(destination, "cancel-final")
	assertAbsent(t, client, destination, temp)
}

func TestE2ERetryAfterCommitIsIdempotentAndNoReplace(t *testing.T) {
	client := e2eClient(t)
	destination := e2eName()
	defer func() { _ = client.share.Remove(destination) }()
	data, digest := randomPayload(t, 1<<20)
	ctx := context.Background()

	if _, err := client.Upload(ctx, "commit-1", destination, digest, bytes.NewReader(data)); err != nil {
		t.Fatalf("first upload: %v", err)
	}
	result, err := client.Upload(ctx, "commit-2", destination, digest, bytes.NewReader(data))
	if err != nil || result.RemoteSHA256 != digest {
		t.Fatalf("retry of committed content should complete, result=%+v err=%v", result, err)
	}
	other, otherDigest := randomPayload(t, 1<<20)
	if _, err := client.Upload(ctx, "commit-3", destination, otherDigest, bytes.NewReader(other)); err == nil {
		t.Fatal("different content replaced an existing destination")
	}
	remoteHash, _, err := readback(ctx, client.share, destination)
	if err != nil || remoteHash != digest {
		t.Fatalf("existing destination changed: hash=%s err=%v", remoteHash, err)
	}
	for _, id := range []string{"commit-1", "commit-2", "commit-3"} {
		temp, _ := temporaryName(destination, id)
		assertAbsent(t, client, temp)
	}
}
