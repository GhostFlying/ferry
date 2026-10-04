package contract

import (
	"encoding/json"
	"fmt"

	"github.com/GhostFlying/ferry/go/core/model"
	"github.com/GhostFlying/ferry/go/planner"
)

type result struct {
	OK    bool               `json:"ok"`
	Error string             `json:"error,omitempty"`
	Files []model.SourceFile `json:"files,omitempty"`
}

// PlanJSON is the narrow JSON boundary used by Android while platform file
// handles remain on the Kotlin side.
func PlanJSON(input string) string {
	var request struct {
		Config model.Config       `json:"config"`
		Files  []model.SourceFile `json:"files"`
	}
	if err := json.Unmarshal([]byte(input), &request); err != nil {
		return encode(result{Error: fmt.Sprintf("invalid request: %v", err)})
	}
	files, err := planner.Plan(request.Config, request.Files)
	if err != nil {
		return encode(result{Error: err.Error()})
	}
	return encode(result{OK: true, Files: files})
}

func ValidateConfigJSON(input string) string {
	var config model.Config
	if err := json.Unmarshal([]byte(input), &config); err != nil {
		return encode(result{Error: fmt.Sprintf("invalid config: %v", err)})
	}
	if err := config.Validate(); err != nil {
		return encode(result{Error: err.Error()})
	}
	return encode(result{OK: true})
}

func encode(value result) string {
	data, err := json.Marshal(value)
	if err != nil {
		return `{"ok":false,"error":"failed to encode result"}`
	}
	return string(data)
}
