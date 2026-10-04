package model

import "fmt"

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
	if c.TargetAddress == "" {
		return fmt.Errorf("target_address is required")
	}
	if c.TimeZone == "" {
		return fmt.Errorf("time_zone is required")
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
	}
	return nil
}
