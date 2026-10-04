package mobilecore

import (
	"context"
	"sync"
	"testing"
	"time"
)

type recordingCallback struct {
	mu     sync.Mutex
	events []string
	done   chan struct{}
}

func newRecordingCallback() *recordingCallback { return &recordingCallback{done: make(chan struct{})} }
func (c *recordingCallback) OnProgress(id string, completed, total int64) { c.mu.Lock(); c.events = append(c.events, "progress"); c.mu.Unlock() }
func (c *recordingCallback) OnComplete(id string) { c.mu.Lock(); c.events = append(c.events, "complete"); c.mu.Unlock(); close(c.done) }
func (c *recordingCallback) OnError(id, code, message string) { c.mu.Lock(); c.events = append(c.events, "error:"+code); c.mu.Unlock(); close(c.done) }
func (c *recordingCallback) snapshot() []string { c.mu.Lock(); defer c.mu.Unlock(); return append([]string(nil), c.events...) }

type scriptedBackend struct { run func(context.Context, Request, func(Progress)) error }
func (b scriptedBackend) Transfer(ctx context.Context, r Request, emit func(Progress)) error { return b.run(ctx, r, emit) }

func waitDone(t *testing.T, c *recordingCallback) { t.Helper(); select { case <-c.done: case <-time.After(time.Second): t.Fatal("callback timeout") } }

func TestBridgeSuccessSerializesTerminalCallback(t *testing.T) {
	b := newBridgeWithBackend(scriptedBackend{run: func(ctx context.Context, r Request, emit func(Progress)) error { emit(Progress{CompletedBytes: 1, TotalBytes: 2}); emit(Progress{CompletedBytes: 2, TotalBytes: 2}); return nil }})
	c := newRecordingCallback()
	if err := b.Start("op-1", 2, c); err != nil { t.Fatal(err) }
	waitDone(t, c)
	got := c.snapshot()
	if len(got) != 3 || got[2] != "complete" { t.Fatalf("events=%v", got) }
}

func TestBridgeCancellationIsTerminalAndIdempotent(t *testing.T) {
	started := make(chan struct{})
	b := newBridgeWithBackend(scriptedBackend{run: func(ctx context.Context, r Request, emit func(Progress)) error { close(started); <-ctx.Done(); emit(Progress{CompletedBytes: 1, TotalBytes: 2}); return ctx.Err() }})
	c := newRecordingCallback()
	if err := b.Start("op-2", 2, c); err != nil { t.Fatal(err) }
	<-started
	if !b.Cancel("op-2") || !b.Cancel("op-2") { t.Fatal("cancel must be idempotent for a live operation") }
	waitDone(t, c)
	got := c.snapshot()
	if len(got) != 1 || got[0] != "error:cancelled" { t.Fatalf("events=%v", got) }
}

func TestBridgeCancellationWinsBeforeBackendFinalization(t *testing.T) {
	started := make(chan struct{})
	release := make(chan struct{})
	b := newBridgeWithBackend(scriptedBackend{run: func(ctx context.Context, r Request, emit func(Progress)) error {
		close(started)
		<-release
		return nil
	}})
	c := newRecordingCallback()
	if err := b.Start("op-race", 1, c); err != nil { t.Fatal(err) }
	<-started
	if !b.Cancel("op-race") { t.Fatal("cancel must win before backend finalization") }
	close(release)
	waitDone(t, c)
	got := c.snapshot()
	if len(got) != 1 || got[0] != "error:cancelled" { t.Fatalf("events=%v", got) }
}

func TestBridgeStructuredErrorAndLateEventIsolation(t *testing.T) {
	b := newBridgeWithBackend(scriptedBackend{run: func(ctx context.Context, r Request, emit func(Progress)) error { emit(Progress{CompletedBytes: 2, TotalBytes: 1}); return &Error{Code: "readback_mismatch", Message: "hash differs"} }})
	c := newRecordingCallback()
	if err := b.Start("op-3", 1, c); err != nil { t.Fatal(err) }
	waitDone(t, c)
	got := c.snapshot()
	if len(got) != 1 || got[0] != "error:readback_mismatch" { t.Fatalf("events=%v", got) }
	if b.Cancel("op-3") { t.Fatal("terminal operation must be removed") }
}

func TestBridgeIgnoresBackendEventAfterTerminal(t *testing.T) {
	var late func(Progress)
	b := newBridgeWithBackend(scriptedBackend{run: func(ctx context.Context, r Request, emit func(Progress)) error {
		late = emit
		return nil
	}})
	c := newRecordingCallback()
	if err := b.Start("op-4", 1, c); err != nil { t.Fatal(err) }
	waitDone(t, c)
	late(Progress{CompletedBytes: 1, TotalBytes: 1})
	time.Sleep(10 * time.Millisecond)
	got := c.snapshot()
	if len(got) != 1 || got[0] != "complete" { t.Fatalf("events=%v", got) }
}

func TestBridgeRejectsInvalidRequests(t *testing.T) {
	b := NewBridge()
	c := newRecordingCallback()
	for _, tc := range []struct{ id string; total int64; want string }{{"", 0, "invalid_operation_id"}, {"x", -1, "invalid_total_bytes"}} {
		if err := b.Start(tc.id, tc.total, c); err == nil || err.Error() != tc.want { t.Fatalf("request=%+v err=%v", tc, err) }
	}
}
