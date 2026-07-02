package com.spoolstudio.app.ui

data class SettingsSaveInput(
    val spoolmanUrl: String,
    val moonrakerUrl: String,
    val spoolmanSortBy: String,
    val bambuMasterKey: String,
    val showCommentField: Boolean
)

data class SettingsSaveState(
    val spoolmanUrl: String,
    val moonrakerUrl: String,
    val spoolmanSortBy: String,
    val bambuMasterKey: String,
    val showCommentField: Boolean
)

data class SettingsLoadState(
    val showLotNumber: Boolean,
    val showCommentField: Boolean,
    val spoolmanUrl: String,
    val spoolmanSortBy: String,
    val moonrakerUrl: String,
    val bambuMasterKey: String
)

fun buildSettingsLoadState(settings: AppSettings): SettingsLoadState =
    SettingsLoadState(
        showLotNumber = settings.showLotNumber,
        showCommentField = settings.showCommentField,
        spoolmanUrl = settings.spoolmanUrl,
        spoolmanSortBy = settings.spoolmanSortBy,
        moonrakerUrl = settings.moonrakerUrl,
        bambuMasterKey = settings.bambuMasterKey
    )

fun buildSettingsSaveState(input: SettingsSaveInput): SettingsSaveState =
    SettingsSaveState(
        spoolmanUrl = normalizeConnectionUrl(input.spoolmanUrl),
        moonrakerUrl = normalizeConnectionUrl(input.moonrakerUrl),
        spoolmanSortBy = input.spoolmanSortBy.ifBlank { "" },
        bambuMasterKey = input.bambuMasterKey.trim().uppercase(),
        showCommentField = input.showCommentField
    )
