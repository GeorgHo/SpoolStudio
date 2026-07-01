package com.spoolstudio.app.ui

import android.util.Log
import com.spoolstudio.app.data.remote.moonraker.MoonrakerService

data class PrinterMappingSnapshot(
    val toolhead1SpoolId: Int?,
    val toolhead2SpoolId: Int?,
    val toolhead3SpoolId: Int?,
    val toolhead4SpoolId: Int?,
    val activeSpoolId: Int?
)

data class PrinterMappingLoadResult(
    val snapshot: PrinterMappingSnapshot,
    val activeSpoolAvailable: Boolean
)

class PrinterMappingRepository(
    private val serviceFactory: (String) -> MoonrakerService = ::MoonrakerService
) {
    suspend fun load(baseUrl: String): PrinterMappingLoadResult {
        val service = serviceFactory(baseUrl)
        val mapping = service.getToolMapping()

        val activeSpoolId = try {
            service.getActiveSpoolId()
        } catch (e: Exception) {
            if (e.message?.contains("404") == true) {
                Log.w("PrinterMappingRepository", "Spoolman not active on printer")
            } else {
                Log.w("PrinterMappingRepository", "Active spool error: ${e.message}")
            }
            null
        }

        return PrinterMappingLoadResult(
            snapshot = mapping.toPrinterMappingSnapshot(activeSpoolId),
            activeSpoolAvailable = activeSpoolId != null
        )
    }

    suspend fun save(
        baseUrl: String,
        toolhead1SpoolId: Int?,
        toolhead2SpoolId: Int?,
        toolhead3SpoolId: Int?,
        toolhead4SpoolId: Int?,
        activeSpoolId: Int?
    ): PrinterMappingSnapshot {
        val service = serviceFactory(baseUrl)

        service.setToolSpool("T0", toolhead1SpoolId)
        service.setToolSpool("T1", toolhead2SpoolId)
        service.setToolSpool("T2", toolhead3SpoolId)
        service.setToolSpool("T3", toolhead4SpoolId)
        service.setActiveSpoolId(activeSpoolId)

        return service.getToolMapping()
            .toPrinterMappingSnapshot(service.getActiveSpoolId())
    }
}

private fun Map<String, Int?>.toPrinterMappingSnapshot(activeSpoolId: Int?): PrinterMappingSnapshot =
    PrinterMappingSnapshot(
        toolhead1SpoolId = this["T0"]?.takeIf { it > 0 },
        toolhead2SpoolId = this["T1"]?.takeIf { it > 0 },
        toolhead3SpoolId = this["T2"]?.takeIf { it > 0 },
        toolhead4SpoolId = this["T3"]?.takeIf { it > 0 },
        activeSpoolId = activeSpoolId
    )
