package com.spoolstudio.app.ui.screens

data class PrinterMappingSelection(
    val toolhead1SpoolId: Int? = null,
    val toolhead2SpoolId: Int? = null,
    val toolhead3SpoolId: Int? = null,
    val toolhead4SpoolId: Int? = null,
    val activeSpoolId: Int? = null
) {
    fun withToolhead1(spoolId: Int?): PrinterMappingSelection =
        copy(toolhead1SpoolId = spoolId).clearActiveIfMissing()

    fun withToolhead2(spoolId: Int?): PrinterMappingSelection =
        copy(toolhead2SpoolId = spoolId).clearActiveIfMissing()

    fun withToolhead3(spoolId: Int?): PrinterMappingSelection =
        copy(toolhead3SpoolId = spoolId).clearActiveIfMissing()

    fun withToolhead4(spoolId: Int?): PrinterMappingSelection =
        copy(toolhead4SpoolId = spoolId).clearActiveIfMissing()

    fun withActiveSpool(spoolId: Int?, checked: Boolean): PrinterMappingSelection =
        copy(activeSpoolId = if (checked) spoolId else null)

    fun clearActiveIfMissing(): PrinterMappingSelection =
        if (activeSpoolId != null && activeSpoolId !in toolheadSpoolIds()) {
            copy(activeSpoolId = null)
        } else {
            this
        }

    fun toolheadSpoolIds(): List<Int?> =
        listOf(toolhead1SpoolId, toolhead2SpoolId, toolhead3SpoolId, toolhead4SpoolId)
}

fun printerMappingSelection(
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    activeSpoolId: Int?
): PrinterMappingSelection =
    PrinterMappingSelection(
        toolhead1SpoolId = toolhead1SpoolId,
        toolhead2SpoolId = toolhead2SpoolId,
        toolhead3SpoolId = toolhead3SpoolId,
        toolhead4SpoolId = toolhead4SpoolId,
        activeSpoolId = activeSpoolId
    )
