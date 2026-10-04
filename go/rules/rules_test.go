package rules

import (
	"testing"

	"github.com/GhostFlying/ferry/go/core/model"
)

func TestEvaluateExcludeWinsOverEarlierInclude(t *testing.T) {
	file := model.SourceFile{Name: "clip.MP4", RelativePath: "DCIM/clip.MP4", Size: 10}
	decision := Evaluate(file, []model.Rule{
		{ID: "include-video", Include: true, Extensions: []string{".mp4"}},
		{ID: "exclude-dcim", Include: false, Prefix: "DCIM/"},
	})
	if decision.Include || decision.RuleID != "exclude-dcim" {
		t.Fatalf("unexpected decision: %+v", decision)
	}
}

func TestEvaluateFirstMatchingInclude(t *testing.T) {
	file := model.SourceFile{Name: "clip.mp4", RelativePath: "DCIM/clip.mp4", Size: 10}
	decision := Evaluate(file, []model.Rule{
		{ID: "first", Include: true, Extensions: []string{".mp4"}},
		{ID: "second", Include: true, Prefix: "DCIM/"},
	})
	if !decision.Include || decision.RuleID != "first" {
		t.Fatalf("unexpected decision: %+v", decision)
	}
}

func TestEvaluateUnknownDefaultsToExclude(t *testing.T) {
	decision := Evaluate(model.SourceFile{Name: "note.txt", Size: 10}, []model.Rule{{ID: "video", Include: true, Extensions: []string{".mp4"}}})
	if decision.Include {
		t.Fatalf("unexpected inclusion: %+v", decision)
	}
}
