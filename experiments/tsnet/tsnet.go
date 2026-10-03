package ferrytsnet

import (
	"context"
	"errors"
	"fmt"
	"net"
	"net/url"
	"os"
	"path/filepath"
	"regexp"
	"sync"

	"tailscale.com/ipn/ipnstate"
	"tailscale.com/tsnet"
)

const defaultHostname = "ferry-m0"

var authURLPattern = regexp.MustCompile(`https://login\.tailscale\.com/[^\s]+`)

type Stage string

const (
	StageStart  Stage = "start"
	StageDial   Stage = "dial"
	StageClose  Stage = "close"
	StageConfig Stage = "config"
)

type StageError struct {
	Stage Stage
	Err   error
}

func (e *StageError) Error() string { return fmt.Sprintf("%s: %v", e.Stage, e.Err) }
func (e *StageError) Unwrap() error { return e.Err }

type Config struct {
	Hostname   string
	Target     string
	AuthKey    string
	OnLoginURL func(string)
	OnStatus   func(string)
}

type backend interface {
	Up(context.Context) (*ipnstate.Status, error)
	Dial(context.Context, string, string) (net.Conn, error)
	Close() error
}

type Client struct {
	backend backend
	dir     string
	status  func(string)

	mu     sync.Mutex
	closed bool
}

func New(cfg Config) (*Client, error) {
	if err := validateConfig(cfg); err != nil {
		return nil, &StageError{Stage: StageConfig, Err: err}
	}
	dir, err := os.MkdirTemp("", "ferry-tsnet-")
	if err != nil {
		return nil, &StageError{Stage: StageConfig, Err: fmt.Errorf("create state directory: %w", err)}
	}
	hostname := cfg.Hostname
	if hostname == "" {
		hostname = defaultHostname
	}
	server := &tsnet.Server{
		Dir:      dir,
		Hostname: hostname,
		AuthKey:  cfg.AuthKey,
		UserLogf: userLogf(cfg.OnLoginURL),
	}
	return &Client{backend: server, dir: dir, status: cfg.OnStatus}, nil
}

func validateConfig(cfg Config) error {
	if cfg.Target == "" {
		return errors.New("target is required")
	}
	host, port, err := net.SplitHostPort(cfg.Target)
	if err != nil || host == "" || port == "" {
		return fmt.Errorf("target must be host:port: %q", cfg.Target)
	}
	if parsed, err := url.Parse("tcp://" + cfg.Target); err != nil || parsed.Host != cfg.Target {
		return fmt.Errorf("target must be a tcp host:port: %q", cfg.Target)
	}
	return nil
}

func userLogf(onLoginURL func(string)) func(string, ...any) {
	return func(format string, args ...any) {
		if onLoginURL == nil {
			return
		}
		message := fmt.Sprintf(format, args...)
		if match := authURLPattern.FindString(message); match != "" {
			onLoginURL(match)
		}
	}
}

func (c *Client) Start(ctx context.Context) error {
	c.mu.Lock()
	if c.closed {
		c.mu.Unlock()
		return &StageError{Stage: StageStart, Err: net.ErrClosed}
	}
	c.mu.Unlock()
	if c.status != nil {
		c.status("starting")
	}
	if _, err := c.backend.Up(ctx); err != nil {
		c.mu.Lock()
		c.closed = true
		c.mu.Unlock()
		_ = c.closeBackend()
		return &StageError{Stage: StageStart, Err: err}
	}
	if c.status != nil {
		c.status("ready")
	}
	return nil
}

func (c *Client) DialSMB(ctx context.Context, target string) (net.Conn, error) {
	if target == "" {
		return nil, &StageError{Stage: StageConfig, Err: errors.New("target is required")}
	}
	if err := validateConfig(Config{Target: target}); err != nil {
		return nil, &StageError{Stage: StageConfig, Err: err}
	}
	c.mu.Lock()
	closed := c.closed
	c.mu.Unlock()
	if closed {
		return nil, &StageError{Stage: StageDial, Err: net.ErrClosed}
	}
	if c.status != nil {
		c.status("dialing")
	}
	conn, err := c.backend.Dial(ctx, "tcp", target)
	if err != nil {
		return nil, &StageError{Stage: StageDial, Err: err}
	}
	return conn, nil
}

func (c *Client) Close() error {
	c.mu.Lock()
	if c.closed {
		c.mu.Unlock()
		return nil
	}
	c.closed = true
	c.mu.Unlock()
	return c.closeBackend()
}

func (c *Client) closeBackend() error {
	var errs []error
	if err := c.backend.Close(); err != nil && !errors.Is(err, net.ErrClosed) {
		errs = append(errs, &StageError{Stage: StageClose, Err: err})
	}
	if c.dir != "" {
		if err := os.RemoveAll(filepath.Clean(c.dir)); err != nil {
			errs = append(errs, &StageError{Stage: StageClose, Err: fmt.Errorf("remove state directory: %w", err)})
		}
	}
	if c.status != nil {
		c.status("closed")
	}
	return errors.Join(errs...)
}

func newClientForTest(b backend, dir string, status func(string)) *Client {
	return &Client{backend: b, dir: dir, status: status}
}
