package planner

import (
	"fmt"
	"path/filepath"

	"github.com/GhostFlying/ferry/go/core/model"
	"github.com/GhostFlying/ferry/go/rules"
)

// Plan selects source files and creates stable private-copy destinations.
func Plan(config model.Config, files []model.SourceFile) ([]model.SourceFile, error) {
	if err := config.Validate(); err != nil {
		return nil, err
	}
	selected := make([]model.SourceFile, 0, len(files))
	for _, file := range files {
		if decision := rules.Evaluate(file, config.Rules); decision.Include {
			file.RelativePath = filepath.ToSlash(file.RelativePath)
			selected = append(selected, file)
		}
	}
	if len(selected) == 0 {
		return nil, fmt.Errorf("no source files matched configured rules")
	}
	return selected, nil
}
