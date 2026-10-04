package rules

import (
	"path/filepath"
	"strings"

	"github.com/GhostFlying/ferry/go/core/model"
)

// Decision is the result of evaluating one source entry.
type Decision struct {
	RuleID  string `json:"rule_id"`
	Include bool   `json:"include"`
	Reason  string `json:"reason"`
}

// Evaluate applies exclusion priority, then the first matching include rule.
// A file with no matching include is excluded by default.
func Evaluate(file model.SourceFile, configured []model.Rule) Decision {
	var firstInclude *model.Rule
	for i := range configured {
		rule := &configured[i]
		if !matches(file, *rule) {
			continue
		}
		if !rule.Include {
			return Decision{RuleID: rule.ID, Include: false, Reason: "excluded by matching rule"}
		}
		if firstInclude == nil {
			firstInclude = rule
		}
	}
	if firstInclude == nil {
		return Decision{Include: false, Reason: "no include rule matched"}
	}
	return Decision{RuleID: firstInclude.ID, Include: true, Reason: "first matching include rule"}
}

func matches(file model.SourceFile, rule model.Rule) bool {
	if rule.Prefix != "" && !strings.HasPrefix(filepath.ToSlash(file.RelativePath), filepath.ToSlash(rule.Prefix)) {
		return false
	}
	if len(rule.Extensions) == 0 {
		return rule.MaxBytes == nil || file.Size <= *rule.MaxBytes
	}
	ext := strings.ToLower(filepath.Ext(file.Name))
	for _, candidate := range rule.Extensions {
		if strings.ToLower(candidate) == ext {
			return rule.MaxBytes == nil || file.Size <= *rule.MaxBytes
		}
	}
	return false
}
