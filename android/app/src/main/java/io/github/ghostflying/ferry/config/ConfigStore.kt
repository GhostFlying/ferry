package io.github.ghostflying.ferry.config

import android.content.SharedPreferences

/** A validated configuration together with the revision recorded on operations. */
data class StoredConfig(val json: String, val revision: Long)

/** Returns null when the configuration is valid, otherwise the rejection reason. */
fun interface ConfigValidator {
    fun validate(configJson: String): String?
}

/**
 * Holds the single M1 configuration and source directory. M1 binds one source,
 * so the persisted tree URI is the binding: it already names the volume, and a
 * persisted grant only resolves while that volume is mounted.
 */
class ConfigStore(
    private val preferences: SharedPreferences,
    private val validator: ConfigValidator,
) {
    val config: StoredConfig?
        get() {
            val json = preferences.getString(KEY_CONFIG, null) ?: return null
            return StoredConfig(json, preferences.getLong(KEY_REVISION, 0))
        }

    val sourceTreeUri: String?
        get() = preferences.getString(KEY_SOURCE_TREE, null)

    /** Saves [configJson] under a new revision, or returns the rejection reason. */
    fun saveConfig(configJson: String): String? {
        validator.validate(configJson)?.let { return it }
        val revision = preferences.getLong(KEY_REVISION, 0) + 1
        preferences.edit()
            .putString(KEY_CONFIG, configJson)
            .putLong(KEY_REVISION, revision)
            .apply()
        return null
    }

    fun saveSourceTree(treeUri: String) {
        preferences.edit().putString(KEY_SOURCE_TREE, treeUri).apply()
    }

    private companion object {
        const val KEY_CONFIG = "config_json"
        const val KEY_REVISION = "config_revision"
        const val KEY_SOURCE_TREE = "source_tree_uri"
    }
}
