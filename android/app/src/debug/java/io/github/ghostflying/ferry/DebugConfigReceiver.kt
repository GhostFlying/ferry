package io.github.ghostflying.ferry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import io.github.ghostflying.ferry.config.BridgeCore
import io.github.ghostflying.ferry.config.ConfigStore

/**
 * Debug-only configuration injection:
 * `adb shell am broadcast -n io.github.ghostflying.ferry/.DebugConfigReceiver --es config_b64 <base64 JSON>`.
 */
class DebugConfigReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val encoded = intent.getStringExtra("config_b64")
        if (encoded == null) {
            Log.w(TAG, "missing config_b64 extra")
            return
        }
        val json = String(Base64.decode(encoded, Base64.DEFAULT), Charsets.UTF_8)
        val rejection = ConfigStore(MainActivity.configPreferences(context), BridgeCore).saveConfig(json)
        if (rejection == null) Log.i(TAG, "configuration saved") else Log.w(TAG, "configuration rejected: $rejection")
    }

    private companion object {
        const val TAG = "FerryDebugConfig"
    }
}
