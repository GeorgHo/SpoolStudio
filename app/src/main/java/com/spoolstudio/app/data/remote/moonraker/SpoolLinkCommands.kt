package com.spoolstudio.app.data.remote.moonraker

/** Clear only the channel mapping. Firmware -24's SET_SPOOL_ID=0 also destroys UID links. */
internal fun spoolLinkAssignmentScripts(toolheadIndex: Int, spoolId: Int): List<String> {
    require(toolheadIndex in 0..3)
    require(spoolId >= 0)
    val setMapping = "SET_PRINT_FILAMENT_CONFIG CONFIG_EXTRUDER=$toolheadIndex FILAMENT_SPOOL_ID=$spoolId FORCE=1"
    return if (spoolId == 0) listOf(setMapping) else listOf(
        setMapping,
        "SET_SPOOL_ID LANE=E$toolheadIndex SPOOL_ID=$spoolId"
    )
}
