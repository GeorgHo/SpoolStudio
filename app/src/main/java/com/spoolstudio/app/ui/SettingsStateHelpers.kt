package com.spoolstudio.app.ui

data class SettingsSaveInput(
    val spoolmanUrl: String,
    val moonrakerUrl: String,
    val printerIntegrationMode: PrinterIntegrationMode,
    val spoolmanSortBy: String,
    val bambuMasterKey: String,
    val showCommentField: Boolean
)

data class SettingsSaveState(
    val spoolmanUrl: String,
    val moonrakerUrl: String,
    val printerIntegrationMode: PrinterIntegrationMode,
    val spoolmanSortBy: String,
    val bambuMasterKey: String,
    val showCommentField: Boolean
)

data class SettingsLoadState(
    val showLotNumber: Boolean,
    val showCommentField: Boolean,
    val showEmptySpoolWeight: Boolean,
    val spoolmanUrl: String,
    val spoolmanSortBy: String,
    val moonrakerUrl: String,
    val printerIntegrationMode: PrinterIntegrationMode,
    val bambuMasterKey: String,
    val materialModifierFieldDeclined: Boolean
)

fun buildSettingsLoadState(settings: AppSettings): SettingsLoadState =
    SettingsLoadState(
        showLotNumber = settings.showLotNumber,
        showCommentField = settings.showCommentField,
        showEmptySpoolWeight = settings.showEmptySpoolWeight,
        spoolmanUrl = settings.spoolmanUrl,
        spoolmanSortBy = settings.spoolmanSortBy,
        moonrakerUrl = settings.moonrakerUrl,
        printerIntegrationMode = settings.printerIntegrationMode,
        bambuMasterKey = settings.bambuMasterKey,
        materialModifierFieldDeclined = settings.materialModifierFieldDeclined
    )

fun buildSettingsSaveState(input: SettingsSaveInput): SettingsSaveState =
    SettingsSaveState(
        spoolmanUrl = normalizeConnectionUrl(input.spoolmanUrl),
        moonrakerUrl = normalizeConnectionUrl(input.moonrakerUrl),
        printerIntegrationMode = input.printerIntegrationMode,
        spoolmanSortBy = input.spoolmanSortBy.ifBlank { "" },
        bambuMasterKey = input.bambuMasterKey.trim().uppercase(),
        showCommentField = input.showCommentField
    )
