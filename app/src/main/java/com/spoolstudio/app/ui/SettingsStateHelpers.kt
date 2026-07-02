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

fun buildSettingsSaveState(input: SettingsSaveInput): SettingsSaveState =
    SettingsSaveState(
        spoolmanUrl = normalizeConnectionUrl(input.spoolmanUrl),
        moonrakerUrl = normalizeConnectionUrl(input.moonrakerUrl),
        spoolmanSortBy = input.spoolmanSortBy.ifBlank { "" },
        bambuMasterKey = input.bambuMasterKey.trim().uppercase(),
        showCommentField = input.showCommentField
    )
