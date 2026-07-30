package com.spoolstudio.app.ui

enum class PrinterIntegrationMode(
    val storedValue: String,
    val label: String
) {
    PAXX12_SPOOL_LINK("paxx12_spoollink", "Firmware Spoolman integration");

    companion object {
        fun fromStoredValue(value: String?): PrinterIntegrationMode =
            entries.firstOrNull { it.storedValue == value } ?: PAXX12_SPOOL_LINK
    }
}

enum class ResolvedPrinterIntegrationMode(
    val label: String
) {
    PAXX12_SPOOL_LINK("Firmware Spoolman integration")
}
