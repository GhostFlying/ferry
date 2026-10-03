package mobilecore

import (
	"context"
	"errors"
	"fmt"
	"sync"
)

const ContractVersion = "m0.bridge.v1"

type Callback interface {
	OnProgress(operationID string, completedBytes int64, totalBytes int64)
	OnComplete(operationID string)
	OnError(operationID string, code string, message string)
}

type Request struct {
	OperationID string
	TotalBytes  int64
}

type Progress struct {
	CompletedBytes int64
	TotalBytes     int64
}

type backend interface {
	Transfer(context.Context, Request, func(Progress)) error
}

type terminalKind uint8

const (
	terminalComplete terminalKind = iota + 1
	terminalCancelled
	terminalError
)

type event struct {
	progress *Progress
	terminal terminalKind
	err      error
}

type operation struct {
	id       string
	cancel   context.CancelFunc
	events   chan event
	done     chan struct{}
	callback Callback
	mu       sync.Mutex
	finished bool
	cancelRequested bool
	delivered bool
}

// Bridge owns operation lifecycle and serializes callbacks for one operation.
type Bridge struct {
	mu      sync.Mutex
	backend backend
	ops     map[string]*operation
}

func NewBridge() *Bridge {
	return &Bridge{backend: unavailableBackend{}, ops: make(map[string]*operation)}
}

func newBridgeWithBackend(b backend) *Bridge {
	return &Bridge{backend: b, ops: make(map[string]*operation)}
}

func (b *Bridge) Start(operationID string, totalBytes int64, callback Callback) error {
	if operationID == "" {
		return errors.New("invalid_operation_id")
	}
	if totalBytes < 0 {
		return errors.New("invalid_total_bytes")
	}
	if callback == nil {
		return errors.New("nil_callback")
	}

	ctx, cancel := context.WithCancel(context.Background())
	op := &operation{id: operationID, cancel: cancel, events: make(chan event, 32), done: make(chan struct{}), callback: callback}
	b.mu.Lock()
	if _, exists := b.ops[operationID]; exists {
		b.mu.Unlock()
		cancel()
		return errors.New("operation_already_exists")
	}
	b.ops[operationID] = op
	backend := b.backend
	b.mu.Unlock()

	go b.dispatch(op)
	go b.run(ctx, op, backend, Request{OperationID: operationID, TotalBytes: totalBytes})
	return nil
}

func (b *Bridge) Cancel(operationID string) bool {
	b.mu.Lock()
	op, ok := b.ops[operationID]
	b.mu.Unlock()
	if !ok {
		return false
	}
	op.mu.Lock()
	op.cancelRequested = true
	op.mu.Unlock()
	op.cancel()
	return true
}

func (b *Bridge) run(ctx context.Context, op *operation, backend backend, request Request) {
	err := backend.Transfer(ctx, request, func(progress Progress) {
		if progress.CompletedBytes < 0 || progress.TotalBytes < 0 || progress.CompletedBytes > progress.TotalBytes {
			return
		}
		op.mu.Lock()
		terminal := op.finished || op.cancelRequested
		op.mu.Unlock()
		if terminal {
			return
		}
		select {
		case <-op.done:
		case op.events <- event{progress: &progress}:
		}
	})

	kind := terminalComplete
	if ctx.Err() != nil {
		kind = terminalCancelled
		err = ctx.Err()
	} else if err != nil {
		kind = terminalError
	}
	op.mu.Lock()
	if op.finished {
		op.mu.Unlock()
		return
	}
	op.finished = true
	op.mu.Unlock()
	select {
	case <-op.done:
	case op.events <- event{terminal: kind, err: err}:
	}
}

func (b *Bridge) dispatch(op *operation) {
	for {
		item := <-op.events
		if item.progress != nil {
			op.mu.Lock()
			terminal := op.delivered
			op.mu.Unlock()
			if terminal {
				continue
			}
			op.callback.OnProgress(op.id, item.progress.CompletedBytes, item.progress.TotalBytes)
			continue
		}
		switch item.terminal {
		case terminalComplete:
			op.callback.OnComplete(op.id)
		case terminalCancelled:
			op.callback.OnError(op.id, "cancelled", "operation cancelled")
		case terminalError:
			code, message := structuredError(item.err)
			op.callback.OnError(op.id, code, message)
		}
		op.mu.Lock()
		op.delivered = true
		op.mu.Unlock()
		close(op.done)
		b.mu.Lock()
		delete(b.ops, op.id)
		b.mu.Unlock()
		return
	}
}

func structuredError(err error) (string, string) {
	if err == nil {
		return "unknown", "operation failed"
	}
	var typed *Error
	if errors.As(err, &typed) {
		return typed.Code, typed.Message
	}
	return "backend_error", err.Error()
}

type Error struct {
	Code    string
	Message string
}

func (e *Error) Error() string { return fmt.Sprintf("%s: %s", e.Code, e.Message) }

type unavailableBackend struct{}

func (unavailableBackend) Transfer(context.Context, Request, func(Progress)) error {
	return &Error{Code: "backend_unavailable", Message: "no transfer backend configured"}
}
