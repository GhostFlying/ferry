package storage

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"io"
	"strings"
	"testing"
)

type fakeRemote struct {
	objects          map[string][]byte
	committed        bool
	readbackOverride string
}

func (f *fakeRemote) CreateExclusive(_ context.Context, name string) (io.WriteCloser, error) {
	if _, exists := f.objects[name]; exists {
		return nil, errors.New("exists")
	}
	return &bufferWriter{close: func(data []byte) error { f.objects[name] = data; return nil }}, nil
}
func (f *fakeRemote) Flush(context.Context, string) error { return nil }
func (f *fakeRemote) ReadBackSHA256(_ context.Context, name string) (string, error) {
	if f.readbackOverride != "" {
		return f.readbackOverride, nil
	}
	digest := sha256.Sum256(f.objects[name])
	return hex.EncodeToString(digest[:]), nil
}
func (f *fakeRemote) CommitNoReplace(_ context.Context, temporaryName, finalName string) error {
	if _, exists := f.objects[finalName]; exists {
		return errors.New("final exists")
	}
	f.objects[finalName] = f.objects[temporaryName]
	f.committed = true
	return nil
}
func (f *fakeRemote) Delete(_ context.Context, name string) error {
	delete(f.objects, name)
	return nil
}

type bufferWriter struct {
	data  []byte
	close func([]byte) error
}

func (w *bufferWriter) Write(data []byte) (int, error) {
	w.data = append(w.data, data...)
	return len(data), nil
}
func (w *bufferWriter) Close() error { return w.close(w.data) }

func TestUploadRequiresRemoteReadbackBeforeCommit(t *testing.T) {
	data := []byte("complete media copy")
	digest := sha256.Sum256(data)
	remote := &fakeRemote{objects: map[string][]byte{}, readbackOverride: "bad"}
	err := Upload(context.Background(), remote, bytes.NewReader(data), hex.EncodeToString(digest[:]), ".tmp", "final.mp4")
	if err == nil || remote.committed {
		t.Fatalf("expected readback failure, err=%v committed=%v", err, remote.committed)
	}
}

func TestUploadRequiresExpectedHash(t *testing.T) {
	remote := &fakeRemote{objects: map[string][]byte{}}
	if err := Upload(context.Background(), remote, bytes.NewBufferString("copy"), "", ".tmp", "final"); err == nil {
		t.Fatal("empty expected hash accepted")
	}
}

func TestUploadRejectsSourceHashMismatchWithoutCommit(t *testing.T) {
	remote := &fakeRemote{objects: map[string][]byte{}}
	err := Upload(context.Background(), remote, bytes.NewBufferString("copy"), strings.Repeat("0", sha256.Size*2), ".tmp", "final")
	if err == nil || remote.committed || len(remote.objects) != 0 {
		t.Fatalf("expected source hash rejection without commit, err=%v remote=%+v", err, remote)
	}
}

func TestUploadLeavesExistingDestinationUnchanged(t *testing.T) {
	data := []byte("complete media copy")
	digest := sha256.Sum256(data)
	remote := &fakeRemote{objects: map[string][]byte{"final.mp4": []byte("existing")}}
	if err := Upload(context.Background(), remote, bytes.NewReader(data), hex.EncodeToString(digest[:]), ".tmp", "final.mp4"); err == nil {
		t.Fatal("expected no-replace failure")
	}
	if string(remote.objects["final.mp4"]) != "existing" || remote.committed {
		t.Fatalf("existing destination changed: %+v", remote.objects)
	}
}

func TestUploadCommitsWithoutReplace(t *testing.T) {
	data := []byte("complete media copy")
	digest := sha256.Sum256(data)
	remote := &fakeRemote{objects: map[string][]byte{}}
	if err := Upload(context.Background(), remote, bytes.NewReader(data), hex.EncodeToString(digest[:]), ".tmp", "final.mp4"); err != nil {
		t.Fatal(err)
	}
	if !remote.committed || string(remote.objects["final.mp4"]) != string(data) {
		t.Fatalf("unexpected remote: %+v", remote)
	}
}
