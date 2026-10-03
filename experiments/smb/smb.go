package smb

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"fmt"
	"io"
	"net"
	"os"
	"path"
	"regexp"
	"strings"

	"github.com/hirochachacha/go-smb2"
)

type Stage string

const (
	StageConnect   Stage = "connect"
	StageAuthenticate Stage = "authenticate"
	StageCreate    Stage = "create"
	StageWrite     Stage = "write"
	StageFlush     Stage = "flush"
	StageRename    Stage = "rename"
	StageReadback  Stage = "readback"
	StageCleanup   Stage = "cleanup"
	StageCancelled Stage = "cancelled"
)

type Error struct {
	Stage Stage
	Err   error
}

func (e *Error) Error() string { return fmt.Sprintf("%s: %v", e.Stage, e.Err) }
func (e *Error) Unwrap() error { return e.Err }

type Result struct {
	Destination string
	Bytes       int64
	LocalSHA256 string
	RemoteSHA256 string
}

type Client struct {
	conn    net.Conn
	session *smb2.Session
	share   *smb2.Share
}

func NewClient(ctx context.Context, conn net.Conn, username, password, shareName string) (*Client, error) {
	if ctx == nil {
		return nil, &Error{Stage: StageConnect, Err: errors.New("nil context")}
	}
	if conn == nil {
		return nil, &Error{Stage: StageConnect, Err: errors.New("nil connection")}
	}
	if shareName == "" {
		return nil, &Error{Stage: StageConnect, Err: errors.New("empty share name")}
	}
	dialer := &smb2.Dialer{Initiator: &smb2.NTLMInitiator{User: username, Password: password}}
	session, err := dialer.DialContext(ctx, conn)
	if err != nil {
		return nil, &Error{Stage: StageAuthenticate, Err: err}
	}
	share, err := session.WithContext(ctx).Mount(shareName)
	if err != nil {
		_ = session.Logoff()
		return nil, &Error{Stage: StageConnect, Err: err}
	}
	return &Client{conn: conn, session: session, share: share}, nil
}

func (c *Client) Close() error {
	var first error
	if c.share != nil {
		first = c.share.Umount()
	}
	if c.session != nil {
		if err := c.session.Logoff(); first == nil {
			first = err
		}
	}
	if c.conn != nil {
		if err := c.conn.Close(); first == nil {
			first = err
		}
	}
	return first
}

var operationIDPattern = regexp.MustCompile(`^[A-Za-z0-9._-]+$`)

func temporaryName(destination, operationID string) (string, error) {
	if err := validateDestination(destination); err != nil {
		return "", err
	}
	if !operationIDPattern.MatchString(operationID) {
		return "", errors.New("invalid operation id")
	}
	return destination + ".ferry-" + operationID + ".part", nil
}

func validateDestination(destination string) error {
	if destination == "" || strings.ContainsRune(destination, '\x00') || strings.Contains(destination, "\\") {
		return errors.New("invalid destination")
	}
	clean := path.Clean(destination)
	if clean == "." || clean != destination || strings.HasPrefix(clean, "../") || strings.HasPrefix(clean, "/") {
		return errors.New("destination must be a relative clean path")
	}
	return nil
}

func (c *Client) Upload(ctx context.Context, operationID, destination string, source io.Reader) (result Result, retErr error) {
	if ctx == nil {
		return Result{}, &Error{Stage: StageCancelled, Err: errors.New("nil context")}
	}
	if source == nil {
		return Result{}, &Error{Stage: StageWrite, Err: errors.New("nil source")}
	}
	temp, err := temporaryName(destination, operationID)
	if err != nil {
		return Result{}, &Error{Stage: StageCreate, Err: err}
	}
	if err := ctx.Err(); err != nil {
		return Result{}, &Error{Stage: StageCancelled, Err: err}
	}
	share := c.share.WithContext(ctx)
	file, err := share.OpenFile(temp, os.O_WRONLY|os.O_CREATE|os.O_EXCL, 0600)
	if err != nil {
		return Result{}, &Error{Stage: StageCreate, Err: err}
	}
	owned := true
	cleanup := func() error {
		if !owned {
			return nil
		}
		return share.Remove(temp)
	}
	defer func() {
		if cleanupErr := cleanup(); cleanupErr != nil && retErr == nil {
			result = Result{}
			retErr = &Error{Stage: StageCleanup, Err: cleanupErr}
		} else if cleanupErr != nil {
			retErr = &Error{Stage: StageCleanup, Err: fmt.Errorf("%v; remove temporary object: %w", retErr, cleanupErr)}
		}
	}()

	hash := sha256.New()
	count, err := copyWithContext(ctx, io.MultiWriter(file, hash), source)
	if err != nil {
		if ctx.Err() != nil {
			return Result{}, &Error{Stage: StageCancelled, Err: ctx.Err()}
		}
		return Result{}, &Error{Stage: StageWrite, Err: err}
	}
	if err := file.Sync(); err != nil {
		return Result{}, &Error{Stage: StageFlush, Err: err}
	}
	if err := file.Close(); err != nil {
		return Result{}, &Error{Stage: StageFlush, Err: err}
	}
	if err := share.Rename(temp, destination); err != nil {
		return Result{}, &Error{Stage: StageRename, Err: err}
	}
	owned = false

	remoteHash, remoteCount, err := readback(ctx, share, destination)
	if err != nil {
		return Result{}, &Error{Stage: StageReadback, Err: err}
	}
	localHash := hex.EncodeToString(hash.Sum(nil))
	if count != remoteCount || localHash != remoteHash {
		return Result{}, &Error{Stage: StageReadback, Err: fmt.Errorf("content mismatch local=%s/%d remote=%s/%d", localHash, count, remoteHash, remoteCount)}
	}
	return Result{Destination: destination, Bytes: count, LocalSHA256: localHash, RemoteSHA256: remoteHash}, nil
}

func copyWithContext(ctx context.Context, dst io.Writer, src io.Reader) (int64, error) {
	buf := make([]byte, 128*1024)
	var count int64
	for {
		if err := ctx.Err(); err != nil {
			return count, err
		}
		n, readErr := src.Read(buf)
		if n > 0 {
			written, err := dst.Write(buf[:n])
			count += int64(written)
			if err != nil {
				return count, err
			}
			if written != n {
				return count, io.ErrShortWrite
			}
		}
		if readErr == io.EOF {
			return count, nil
		}
		if readErr != nil {
			return count, readErr
		}
	}
}

func readback(ctx context.Context, share *smb2.Share, destination string) (hash string, count int64, retErr error) {
	file, err := share.OpenFile(destination, os.O_RDONLY, 0)
	if err != nil {
		return "", 0, err
	}
	defer func() {
		if closeErr := file.Close(); closeErr != nil && retErr == nil {
			retErr = closeErr
		}
	}()
	digest := sha256.New()
	count, err := copyWithContext(ctx, digest, file)
	if err != nil {
		return "", count, err
	}
	return hex.EncodeToString(digest.Sum(nil)), count, nil
}
