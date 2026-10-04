package model

import "testing"

func TestConfigRejectsMalformedTargetAndTimezone(t *testing.T) {
	base := Config{Version: 1, SourceName: "Pocket", TargetName: "fnOS", TargetAddress: "smb://nas/share", TimeZone: "Asia/Shanghai"}
	for name, config := range map[string]Config{
		"address":  {Version: base.Version, SourceName: base.SourceName, TargetName: base.TargetName, TargetAddress: "https://nas/share", TimeZone: base.TimeZone},
		"timezone": {Version: base.Version, SourceName: base.SourceName, TargetName: base.TargetName, TargetAddress: base.TargetAddress, TimeZone: "Mars/Olympus"},
	} {
		if err := config.Validate(); err == nil {
			t.Fatalf("%s was accepted", name)
		}
	}
}

func TestSourceRejectsTraversalDuplicateInputs(t *testing.T) {
	for _, file := range []SourceFile{{Name: "x.mp4", RelativePath: "../x.mp4"}, {Name: "x.mp4", RelativePath: "/x.mp4"}, {Name: "x.mp4", RelativePath: "DCIM//x.mp4"}, {Name: "x.mp4", RelativePath: "DCIM/x.mp4", Size: -1}} {
		if err := file.Validate(); err == nil {
			t.Fatalf("unsafe source accepted: %+v", file)
		}
	}
}
