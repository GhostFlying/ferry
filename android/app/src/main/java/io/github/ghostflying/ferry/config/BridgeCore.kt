package io.github.ghostflying.ferry.config

import io.github.ghostflying.ferry.source.Planner
import io.github.ghostflying.ferry.source.SourceEntry
import org.json.JSONArray
import org.json.JSONObject

/** Adapts the gomobile bridge to the Kotlin interfaces used by the app. */
object BridgeCore : ConfigValidator, Planner {
    // planner.Plan reports an empty match as an error; for a scan it is simply
    // nothing to import.
    private const val NO_MATCH = "no source files matched configured rules"

    override fun validate(configJson: String): String? {
        val result = JSONObject(bridge.Bridge.validateConfigJSON(configJson))
        return if (result.optBoolean("ok")) null else result.optString("error", "invalid config")
    }

    override fun plan(configJson: String, files: List<SourceEntry>): Set<String> {
        val request = JSONObject()
            .put("config", JSONObject(configJson))
            .put(
                "files",
                JSONArray(
                    files.map {
                        JSONObject()
                            .put("name", it.name)
                            .put("relative_path", it.relativePath)
                            .put("size", it.size)
                            .put("modified_at", it.modifiedAt.toString())
                    },
                ),
            )
        val result = JSONObject(bridge.Bridge.planJSON(request.toString()))
        if (!result.optBoolean("ok")) {
            val error = result.optString("error")
            if (error == NO_MATCH) return emptySet()
            error("rule planning failed: $error")
        }
        val planned = result.optJSONArray("files") ?: return emptySet()
        return (0 until planned.length()).mapTo(mutableSetOf()) {
            planned.getJSONObject(it).getString("relative_path")
        }
    }
}
