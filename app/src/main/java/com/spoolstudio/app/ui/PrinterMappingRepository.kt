package com.spoolstudio.app.ui

import com.spoolstudio.app.data.remote.moonraker.MoonrakerService

data class PrinterMappingSnapshot(
    val toolhead1SpoolId: Int?,
    val toolhead2SpoolId: Int?,
    val toolhead3SpoolId: Int?,
    val toolhead4SpoolId: Int?,
    val activeSpoolId: Int?,
    val integrationMode: ResolvedPrinterIntegrationMode
)

data class PrinterMappingLoadResult(
    val snapshot: PrinterMappingSnapshot,
    val activeSpoolAvailable: Boolean
)

class PrinterMappingRepository(
    private val serviceFactory: (String) -> MoonrakerService = ::MoonrakerService
) {
    suspend fun load(
        baseUrl: String,
        printerIntegrationMode: PrinterIntegrationMode
    ): PrinterMappingLoadResult {
        val service = serviceFactory(baseUrl)
        ensureSpoolLinkFirmware(service)
        val mapping = service.getSpoolLinkToolMapping()

        return PrinterMappingLoadResult(
            snapshot = mapping.toPrinterMappingSnapshot(
                activeSpoolId = null,
                integrationMode = ResolvedPrinterIntegrationMode.PAXX12_SPOOL_LINK
            ),
            activeSpoolAvailable = false
        )
    }

    suspend fun save(
        baseUrl: String,
        toolhead1SpoolId: Int?,
        toolhead2SpoolId: Int?,
        toolhead3SpoolId: Int?,
        toolhead4SpoolId: Int?,
        activeSpoolId: Int?,
        printerIntegrationMode: PrinterIntegrationMode
    ): PrinterMappingSnapshot {
        val service = serviceFactory(baseUrl)
        ensureSpoolLinkFirmware(service)

        service.setSpoolLinkToolSpool(0, toolhead1SpoolId)
        service.setSpoolLinkToolSpool(1, toolhead2SpoolId)
        service.setSpoolLinkToolSpool(2, toolhead3SpoolId)
        service.setSpoolLinkToolSpool(3, toolhead4SpoolId)

        return service.getSpoolLinkToolMapping()
            .toPrinterMappingSnapshot(
                activeSpoolId = null,
                integrationMode = ResolvedPrinterIntegrationMode.PAXX12_SPOOL_LINK
            )
    }

    suspend fun assignToolhead(
        baseUrl: String,
        toolheadIndex: Int,
        spoolId: Int?,
        printerIntegrationMode: PrinterIntegrationMode
    ): PrinterMappingSnapshot {
        val service = serviceFactory(baseUrl)
        ensureSpoolLinkFirmware(service)
        val mapping = service.setSpoolLinkToolSpool(toolheadIndex, spoolId)

        return mapping
            .toPrinterMappingSnapshot(
                activeSpoolId = null,
                integrationMode = ResolvedPrinterIntegrationMode.PAXX12_SPOOL_LINK
            )
    }

    private suspend fun ensureSpoolLinkFirmware(service: MoonrakerService) {
        val printerInfo = service.getPrinterInfo()
        if (!printerInfo.supportsSpoolLink) {
            val version = printerInfo.displayVersion ?: "unknown"
            throw IllegalStateException(
                "Paxx12 SpoolLink requires firmware 1.5.0 or newer. Detected firmware: $version. Use Spool Studio v2 for the old Paxx12 workflow."
            )
        }
        if (
            printerInfo.hasSpoolmanComponent == false ||
            printerInfo.hasSpoolLinkComponent == false ||
            printerInfo.spoolmanIntegrationEnabled == false ||
            printerInfo.assignmentCommandAvailable != true
        ) {
            throw IllegalStateException(
                "Printer Spoolman integration is not ready. Enable Spoolman Integration in the printer config, then restart Klipper/Moonraker."
            )
        }
    }
}

private fun Map<String, Int?>.toPrinterMappingSnapshot(
    activeSpoolId: Int?,
    integrationMode: ResolvedPrinterIntegrationMode
): PrinterMappingSnapshot =
    PrinterMappingSnapshot(
        toolhead1SpoolId = this["T0"]?.takeIf { it > 0 },
        toolhead2SpoolId = this["T1"]?.takeIf { it > 0 },
        toolhead3SpoolId = this["T2"]?.takeIf { it > 0 },
        toolhead4SpoolId = this["T3"]?.takeIf { it > 0 },
        activeSpoolId = activeSpoolId,
        integrationMode = integrationMode
    )
