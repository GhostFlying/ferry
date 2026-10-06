package model

import "time"

// OperationPhase is the durable phase of a single media operation.
type OperationPhase string

const (
	PhaseImported  OperationPhase = "imported"
	PhaseUploading OperationPhase = "uploading"
	PhaseVerifying OperationPhase = "verifying"
	PhaseWaiting   OperationPhase = "waiting"
	PhasePaused    OperationPhase = "paused"
	PhaseFailed    OperationPhase = "failed"
	PhaseCompleted OperationPhase = "completed"
)

// Rule describes one deterministic path rule. Exclude rules always win over
// include rules; among includes, the first matching rule wins.
type Rule struct {
	ID         string   `json:"id"`
	Include    bool     `json:"include"`
	Extensions []string `json:"extensions,omitempty"`
	Prefix     string   `json:"prefix,omitempty"`
	MaxBytes   *int64   `json:"max_bytes,omitempty"`
}

// Config is the versioned Android configuration exchanged with the bridge.
type Config struct {
	Version       int    `json:"version"`
	SourceName    string `json:"source_name"`
	TargetName    string `json:"target_name"`
	TargetAddress string `json:"target_address"`
	Rules         []Rule `json:"rules"`
	TimeZone      string `json:"time_zone"`
}

// SourceFile is a read-only source entry before it becomes a private copy.
type SourceFile struct {
	Name         string    `json:"name"`
	RelativePath string    `json:"relative_path"`
	Size         int64     `json:"size"`
	ModifiedAt   time.Time `json:"modified_at"`
	SHA256       string    `json:"sha256,omitempty"`
}

// OperationState is persisted before external work and after each verified
// transition. Revision prevents a resumed task from moving to a newer config.
type OperationState struct {
	ID           string         `json:"id"`
	Revision     int64          `json:"revision"`
	Phase        OperationPhase `json:"phase"`
	ManualPaused bool           `json:"manual_paused"`
	SourcePath   string         `json:"source_path"`
	PrivateCopy  string         `json:"private_copy"`
	SourceSHA256 string         `json:"source_sha256"`
	RemoteSHA256 string         `json:"remote_sha256,omitempty"`
	LastError    string         `json:"last_error,omitempty"`
	UpdatedAt    time.Time      `json:"updated_at"`
}
