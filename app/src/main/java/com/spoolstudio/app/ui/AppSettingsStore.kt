package com.spoolstudio.app.ui

import android.content.Context

data class AppSettings(
    val spoolmanUrl: String,
    val moonrakerUrl: String,
    val spoolmanSortBy: String,
    val bambuMasterKey: String,
    val showLotNumber: Boolean,
    val showCommentField: Boolean,
    val showEmptySpoolWeight: Boolean
)

object AppSettingsStore {
    private const val PREFS_NAME = "spoolstudio_prefs"
    private const val SPOOLMAN_URL_KEY = "spoolman_url"
    private const val SPOOLMAN_SORT_KEY = "spoolman_sort"
    private const val DEFAULT_URL = ""
    private const val MOONRAKER_URL_KEY = "moonraker_url"
    private const val SHOW_LOT_NUMBER_KEY = "show_lot_number"
    private const val SHOW_COMMENT_FIELD = "show_comment_field"
    private const val SHOW_EMPTY_SPOOL_WEIGHT = "show_empty_spool_weight"
    private const val DEFAULT_MOONRAKER_URL = ""
    private const val BAMBU_MASTER_KEY = "bambu_master_key"

    fun load(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return AppSettings(
            spoolmanUrl = normalizeUrl(prefs.getString(SPOOLMAN_URL_KEY, DEFAULT_URL) ?: DEFAULT_URL),
            moonrakerUrl = normalizeUrl(
                prefs.getString(MOONRAKER_URL_KEY, DEFAULT_MOONRAKER_URL)
                    ?: DEFAULT_MOONRAKER_URL
            ),
            spoolmanSortBy = prefs.getString(SPOOLMAN_SORT_KEY, "")?.ifBlank { "" } ?: "",
            bambuMasterKey = prefs.getString(BAMBU_MASTER_KEY, "")?.trim()?.uppercase() ?: "",
            showLotNumber = prefs.getBoolean(SHOW_LOT_NUMBER_KEY, false),
            showCommentField = prefs.getBoolean(SHOW_COMMENT_FIELD, false),
            showEmptySpoolWeight = prefs.getBoolean(SHOW_EMPTY_SPOOL_WEIGHT, false)
        )
    }

    fun saveConnectionSettings(
        context: Context,
        spoolmanUrl: String,
        moonrakerUrl: String,
        spoolmanSortBy: String,
        bambuMasterKey: String,
        showCommentField: Boolean
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SPOOLMAN_URL_KEY, normalizeUrl(spoolmanUrl))
            .putString(MOONRAKER_URL_KEY, normalizeUrl(moonrakerUrl))
            .putString(SPOOLMAN_SORT_KEY, spoolmanSortBy.ifBlank { "" })
            .putString(BAMBU_MASTER_KEY, bambuMasterKey.trim().uppercase())
            .putBoolean(SHOW_COMMENT_FIELD, showCommentField)
            .apply()
    }

    fun saveShowLotNumber(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(SHOW_LOT_NUMBER_KEY, value)
            .apply()
    }

    fun saveShowEmptySpoolWeight(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(SHOW_EMPTY_SPOOL_WEIGHT, value)
            .apply()
    }

    private fun normalizeUrl(url: String): String = url.trim().removeSuffix("/")
}
