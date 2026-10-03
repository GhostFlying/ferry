package ferrytsnet

import (
	"context"
	"errors"
	"io"
	"net"
	"os"
	"path/filepath"
	"reflect"
	"testing"
	"time"

	"tailscale.com/ipn/ipnstate"
)

type fakeBackend struct {
	upErr      error
	dialErr    error
	closeErr   error
	upCalls    int
	dialAddr   string
	closeCalls int
}

func (f *fakeBackend) Up(context.Context) (*ipnstate.Status, error) {
	f.upCalls++
	return &ipnstate.Status{}, f.upErr
}
func (f *fakeBackend) Dial(_ context.Context, network, address string) (net.Conn, error) {
	f.dialAddr = network + ":" + address
	if f.dialErr != nil {
		return nil, f.dialErr
	}
	return &fakeConn{}, nil
}
func (f *fakeBackend) Close() error { f.closeCalls++; return f.closeErr }

type fakeConn struct{}

func (fakeConn) Read([]byte) (int, error)         { return 0, io.EOF }
func (fakeConn) Write(p []byte) (int, error)      { return len(p), nil }
func (fakeConn) Close() error                     { return nil }
func (fakeConn) LocalAddr() net.Addr              { return fakeAddr("local") }
func (fakeConn) RemoteAddr() net.Addr             { return fakeAddr("remote") }
func (fakeConn) SetDeadline(time.Time) error      { return nil }
func (fakeConn) SetReadDeadline(time.Time) error  { return nil }
func (fakeConn) SetWriteDeadline(time.Time) error { return nil }

type fakeAddr string

func (a fakeAddr) Network() string { return "fake" }
func (a fakeAddr) String() string  { return string(a) }

func TestValidateConfig(t *testing.T) {
	for _, target := range []string{"", "host", "host:notaport", ":445", "host:445/path"} {
		if err := validateConfig(Config{Target: target}); err == nil {
			t.Errorf("validateConfig(%q) succeeded", target)
		}
	}
	if err := validateConfig(Config{Target: "host:445"}); err != nil {
		t.Fatalf("valid target rejected: %v", err)
	}
}

func TestLifecycleAndDial(t *testing.T) {
	dir := t.TempDir()
	fake := &fakeBackend{}
	var statuses []string
	client := newClientForTest(fake, dir, "server:445", func(status string) { statuses = append(statuses, status) })
	if err := client.Start(context.Background()); err != nil {
		t.Fatalf("Start: %v", err)
	}
	conn, err := client.DialSMB(context.Background(), "server:445")
	if err != nil {
		t.Fatalf("DialSMB: %v", err)
	}
	_ = conn.Close()
	if err := client.Close(); err != nil {
		t.Fatalf("Close: %v", err)
	}
	if got, want := fake.dialAddr, "tcp:server:445"; got != want {
		t.Fatalf("dial address = %q, want %q", got, want)
	}
	if got, want := statuses, []string{"starting", "ready", "dialing", "closed"}; !reflect.DeepEqual(got, want) {
		t.Fatalf("statuses = %#v, want %#v", got, want)
	}
	if fake.closeCalls != 1 {
		t.Fatalf("close calls = %d, want 1", fake.closeCalls)
	}
	if err := client.Close(); err != nil {
		t.Fatalf("second Close: %v", err)
	}
}

func TestDialRequiresStartedConfiguredTargetAndContext(t *testing.T) {
	client := newClientForTest(&fakeBackend{}, t.TempDir(), "server:445", nil)
	if _, err := client.DialSMB(context.Background(), "other:445"); err == nil {
		t.Fatal("DialSMB accepted an alternate endpoint")
	}
	if _, err := client.DialSMB(context.Background(), "server:445"); err == nil {
		t.Fatal("DialSMB started an unstarted client")
	}
	if err := client.Start(nil); err == nil {
		t.Fatal("Start accepted nil context")
	}
	if _, err := client.DialSMB(nil, "server:445"); err == nil {
		t.Fatal("DialSMB accepted nil context")
	}
}

func TestCloseBeforeStartOnlyRemovesState(t *testing.T) {
	dir := filepath.Join(t.TempDir(), "state")
	if err := os.Mkdir(dir, 0o700); err != nil {
		t.Fatal(err)
	}
	fake := &fakeBackend{}
	client := newClientForTest(fake, dir, "server:445", nil)
	if err := client.Close(); err != nil {
		t.Fatalf("Close: %v", err)
	}
	if fake.closeCalls != 0 {
		t.Fatalf("close calls = %d, want 0", fake.closeCalls)
	}
	if _, err := os.Stat(dir); !errors.Is(err, os.ErrNotExist) {
		t.Fatalf("state dir still exists: %v", err)
	}
}

func TestStartErrorClosesAndRemovesState(t *testing.T) {
	dir := filepath.Join(t.TempDir(), "state")
	if err := os.Mkdir(dir, 0o700); err != nil {
		t.Fatal(err)
	}
	fake := &fakeBackend{upErr: errors.New("login failed")}
	client := newClientForTest(fake, dir, "server:445", nil)
	err := client.Start(context.Background())
	var stageErr *StageError
	if !errors.As(err, &stageErr) || stageErr.Stage != StageStart {
		t.Fatalf("Start error = %v", err)
	}
	if fake.closeCalls != 1 {
		t.Fatalf("close calls = %d, want 1", fake.closeCalls)
	}
	if _, err := os.Stat(dir); !errors.Is(err, os.ErrNotExist) {
		t.Fatalf("state dir still exists: %v", err)
	}
}

func TestUserLogfForwardsOnlyLoginURL(t *testing.T) {
	var got string
	logf := userLogf(func(url string) { got = url })
	logf("noise auth key=%s", "tskey-secret")
	if got != "" {
		t.Fatalf("unexpected callback for non-URL log: %q", got)
	}
	logf("To authenticate, visit https://login.tailscale.com/a/test?foo=bar now")
	if got != "https://login.tailscale.com/a/test?foo=bar" {
		t.Fatalf("url = %q", got)
	}
}
