package storage

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"regexp"
	"strings"
	"time"
)

var sha256Pattern = regexp.MustCompile(`^[0-9a-fA-F]{64}$`)

// RemoteObject is the minimal no-replace contract. Implementations may use
// SMB, tsnet or a test server, but must expose readback rather than trusting a
// successful write.
type RemoteObject interface {
	CreateExclusive(ctx context.Context, name string) (io.WriteCloser, error)
	Flush(ctx context.Context, name string) error
	ReadBackSHA256(ctx context.Context, name string) (string, error)
	CommitNoReplace(ctx context.Context, temporaryName, finalName string) error
	Delete(ctx context.Context, name string) error
}

// deleteTemporary removes an abandoned temporary object even when ctx has
// been cancelled; otherwise the next CreateExclusive for it would fail.
func deleteTemporary(ctx context.Context, remote RemoteObject, name string) {
	cleanupCtx, cancel := context.WithTimeout(context.WithoutCancel(ctx), 30*time.Second)
	defer cancel()
	_ = remote.Delete(cleanupCtx, name)
}

// Upload streams a complete private copy to a temporary object and only
// reports completion after readback and server-side no-replace succeed.
func Upload(ctx context.Context, remote RemoteObject, source io.Reader, expectedSHA256, temporaryName, finalName string) error {
	if !sha256Pattern.MatchString(expectedSHA256) {
		return fmt.Errorf("expected SHA-256 is required before remote completion")
	}
	expectedSHA256 = strings.ToLower(expectedSHA256)
	writer, err := remote.CreateExclusive(ctx, temporaryName)
	if err != nil {
		return fmt.Errorf("create temporary object: %w", err)
	}
	digest := sha256.New()
	_, copyErr := io.CopyBuffer(io.MultiWriter(writer, digest), source, make([]byte, 1024*1024))
	closeErr := writer.Close()
	if copyErr != nil {
		deleteTemporary(ctx, remote, temporaryName)
		return fmt.Errorf("write temporary object: %w", copyErr)
	}
	if closeErr != nil {
		deleteTemporary(ctx, remote, temporaryName)
		return fmt.Errorf("close temporary object: %w", closeErr)
	}
	if err := remote.Flush(ctx, temporaryName); err != nil {
		deleteTemporary(ctx, remote, temporaryName)
		return fmt.Errorf("flush temporary object: %w", err)
	}
	localSHA := hex.EncodeToString(digest.Sum(nil))
	if expectedSHA256 != "" && localSHA != expectedSHA256 {
		deleteTemporary(ctx, remote, temporaryName)
		return fmt.Errorf("source hash mismatch: got %s want %s", localSHA, expectedSHA256)
	}
	remoteSHA, err := remote.ReadBackSHA256(ctx, temporaryName)
	if err != nil {
		deleteTemporary(ctx, remote, temporaryName)
		return fmt.Errorf("remote readback: %w", err)
	}
	if strings.ToLower(remoteSHA) != localSHA {
		deleteTemporary(ctx, remote, temporaryName)
		return fmt.Errorf("remote hash mismatch: got %s want %s", remoteSHA, localSHA)
	}
	if err := remote.CommitNoReplace(ctx, temporaryName, finalName); err != nil {
		deleteTemporary(ctx, remote, temporaryName)
		return fmt.Errorf("commit without replace: %w", err)
	}
	return nil
}
