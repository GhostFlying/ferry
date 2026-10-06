package model

import (
	"fmt"
	"net/url"
	"path"
	"strings"
	"time"
)

// Validate checks fields that are required before an operation can be
// planned. It deliberately does not test network reachability or credentials.
func (c Config) Validate() error {
	if c.Version != 1 {
		return fmt.Errorf("config version must be 1")
	}
	if c.SourceName == "" {
		return fmt.Errorf("source_name is required")
	}
	if c.TargetName == "" {
		return fmt.Errorf("target_name is required")
	}
	if err := validateTargetAddress(c.TargetAddress); err != nil {
		return err
	}
	if _, err := time.LoadLocation(c.TimeZone); err != nil {
		return fmt.Errorf("invalid time_zone: %w", err)
	}
	seen := make(map[string]struct{}, len(c.Rules))
	for _, rule := range c.Rules {
		if rule.ID == "" {
			return fmt.Errorf("rule id is required")
		}
		if _, ok := seen[rule.ID]; ok {
			return fmt.Errorf("duplicate rule id %q", rule.ID)
		}
		seen[rule.ID] = struct{}{}
		if rule.MaxBytes != nil && *rule.MaxBytes < 0 {
			return fmt.Errorf("rule %q has negative max_bytes", rule.ID)
		}
		if rule.Prefix != "" {
			if _, err := CanonicalRelativePath(strings.TrimSuffix(rule.Prefix, "/")); err != nil {
				return fmt.Errorf("rule %q prefix: %w", rule.ID, err)
			}
		}
		for _, extension := range rule.Extensions {
			if extension == "" || !strings.HasPrefix(extension, ".") || strings.ContainsAny(extension, `/\\`) {
				return fmt.Errorf("rule %q has invalid extension %q", rule.ID, extension)
			}
		}
	}
	return nil
}

func validateTargetAddress(raw string) error {
	parsed, err := url.Parse(raw)
	if err != nil || parsed.Scheme != "smb" || parsed.Host == "" || parsed.User != nil || parsed.RawQuery != "" || parsed.Fragment != "" {
		return fmt.Errorf("target_address must be an smb:// host without credentials or query: %q", raw)
	}
	return nil
}

// CanonicalRelativePath rejects absolute paths, traversal, empty segments and
// platform separators before a path is used for a private copy or remote key.
func CanonicalRelativePath(raw string) (string, error) {
	if raw == "" || strings.ContainsRune(raw, '\x00') || strings.Contains(raw, `\`) || strings.HasPrefix(raw, "/") {
		return "", fmt.Errorf("path must be a non-empty relative slash path")
	}
	clean := path.Clean(raw)
	if clean == "." || clean != raw || strings.HasPrefix(clean, "../") || strings.Contains(clean, "//") {
		return "", fmt.Errorf("path is not canonical and relative: %q", raw)
	}
	for _, segment := range strings.Split(clean, "/") {
		if segment == "" || segment == "." || segment == ".." {
			return "", fmt.Errorf("path contains unsafe segment: %q", raw)
		}
	}
	return clean, nil
}

func (f SourceFile) Validate() error {
	if _, err := CanonicalRelativePath(f.RelativePath); err != nil {
		return err
	}
	if f.Name == "" || strings.ContainsAny(f.Name, `/\\`) {
		return fmt.Errorf("invalid source name: %q", f.Name)
	}
	if f.Size < 0 {
		return fmt.Errorf("source size cannot be negative")
	}
	return nil
}
