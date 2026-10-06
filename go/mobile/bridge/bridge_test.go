package bridge

import "testing"

func TestValidateConfigJSON(t *testing.T) {
	got := ValidateConfigJSON(`{"version":1,"source_name":"Pocket","target_name":"fnOS","target_address":"smb://test","time_zone":"Asia/Shanghai","rules":[]}`)
	if got != `{"ok":true}` {
		t.Fatalf("unexpected result: %s", got)
	}
}
