package smb

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"fmt"
	"net"
	"os"
	"strings"
	"testing"
	"time"
)

// TestControlledServerReadbackAndNoReplace is opt-in so ordinary unit tests do
// not require a network service or credentials. The service must be a real SMB
// server prepared for this verification run, not an in-memory fake.
func TestControlledServerReadbackAndNoReplace(t *testing.T) {
	if os.Getenv("FERRY_SMB_E2E") != "1" {
		t.Skip("set FERRY_SMB_E2E=1 to run against the controlled SMB server")
	}
	target := requiredEnv(t, "FERRY_SMB_TARGET")
	username := requiredEnv(t, "FERRY_SMB_USER")
	password := requiredEnv(t, "FERRY_SMB_PASSWORD")
	shareName := requiredEnv(t, "FERRY_SMB_SHARE")

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()
	conn, err := (&net.Dialer{}).DialContext(ctx, "tcp", target)
	if err != nil {
		t.Fatalf("dial controlled SMB service: %v", err)
	}
	client, err := NewClient(ctx, conn, username, password, shareName)
	if err != nil {
		_ = conn.Close()
		t.Fatalf("open controlled SMB service: %v", err)
	}
	defer func() {
		if err := client.Close(); err != nil {
			t.Errorf("close SMB client: %v", err)
		}
	}()

	destination := "ferry-e2e-" + strings.ReplaceAll(fmt.Sprint(time.Now().UnixNano()), "-", "") + ".bin"
	defer func() {
		if err := client.share.Remove(destination); err != nil {
			t.Errorf("remove test destination: %v", err)
		}
	}()
	first := []byte("controlled-smb-readback\n")
	firstHash := sha256.Sum256(first)
	result, err := client.Upload(ctx, "readback-1", destination, strings.NewReader(string(first)))
	if err != nil {
		t.Fatalf("upload first object: %v", err)
	}
	if result.Bytes != int64(len(first)) || result.LocalSHA256 != hex.EncodeToString(firstHash[:]) || result.RemoteSHA256 != result.LocalSHA256 {
		t.Fatalf("unexpected first result: %+v", result)
	}
	t.Logf("readback destination=%s bytes=%d local_sha256=%s remote_sha256=%s", destination, result.Bytes, result.LocalSHA256, result.RemoteSHA256)

	second := []byte("must-not-replace\n")
	if _, err := client.Upload(ctx, "readback-2", destination, strings.NewReader(string(second))); err == nil {
		t.Fatal("expected existing destination to reject replacement")
	} else {
		var stageErr *Error
		if !errors.As(err, &stageErr) || stageErr.Stage != StageRename {
			t.Fatalf("replacement error stage=%v", err)
		}
	}

	remoteHash, remoteCount, err := readback(ctx, client.share, destination)
	if err != nil {
		t.Fatalf("read back preserved destination: %v", err)
	}
	if remoteCount != int64(len(first)) || remoteHash != hex.EncodeToString(firstHash[:]) {
		t.Fatalf("existing destination changed: hash=%s bytes=%d", remoteHash, remoteCount)
	}
	t.Logf("no_replace_preserved bytes=%d remote_sha256=%s", remoteCount, remoteHash)

}

func requiredEnv(t *testing.T, name string) string {
	t.Helper()
	value := os.Getenv(name)
	if value == "" {
		t.Fatalf("%s is required", name)
	}
	return value
}
