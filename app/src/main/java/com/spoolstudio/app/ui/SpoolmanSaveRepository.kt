package com.spoolstudio.app.ui

import com.spoolstudio.app.data.remote.spoolman.SpoolmanService
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData

data class SpoolmanSaveInput(
    val baseUrl: String,
    val request: SpoolmanSaveRequest,
    val mode: SpoolMode,
    val selectedSpool: FilamentSpool?,
    val readData: OpenSpoolData?,
    val currentSpoolId: String?
)

data class SpoolmanSaveResult(
    val finalSpool: FilamentSpool,
    val tagData: OpenSpoolData?,
    val resolvedLotNr: String,
    val actionMode: SpoolMode
)

class SpoolmanSaveRepository(
    private val serviceFactory: (String) -> SpoolmanService = ::SpoolmanService
) {
    suspend fun save(input: SpoolmanSaveInput): SpoolmanSaveResult {
        val service = serviceFactory(input.baseUrl)
        val request = input.request
        val actionMode = input.mode

        val resolvedLotNr = when (actionMode) {
            SpoolMode.UPDATE -> request.lotNr.trim().ifBlank {
                input.selectedSpool?.lotNr
                    ?: input.readData?.lotNr
                    ?: ""
            }
            SpoolMode.CREATE, SpoolMode.DUPLICATE -> request.lotNr.trim()
        }

        val vendor = service.createOrFindVendor(request.brand)
        val composedMaterial = buildMaterialWithVariant(request.material, request.variant)
        val cleanColorName = request.colorName.trim().ifBlank { "Unknown" }
        val cleanColorHex = normalizedColorHex(request.colorHex)
        val nozzleTemp = request.minTemp.toIntOrNull()
        val bedTemp = request.bedMinTemp.toIntOrNull()
        val cleanRemainingWeight = parseRemainingWeight(request.remainingWeight)
        val cleanEmptySpoolWeight = parseRemainingWeight(request.emptySpoolWeight)

        val finalSpool = when (actionMode) {
            SpoolMode.UPDATE -> updateSpool(
                service = service,
                input = input,
                resolvedLotNr = resolvedLotNr,
                vendorId = vendor.id ?: throw IllegalStateException("Vendor id missing"),
                composedMaterial = composedMaterial,
                cleanColorName = cleanColorName,
                cleanColorHex = cleanColorHex,
                nozzleTemp = nozzleTemp,
                bedTemp = bedTemp,
                cleanRemainingWeight = cleanRemainingWeight,
                cleanEmptySpoolWeight = cleanEmptySpoolWeight
            )

            SpoolMode.CREATE, SpoolMode.DUPLICATE -> createSpool(
                service = service,
                input = input,
                resolvedLotNr = resolvedLotNr,
                vendorId = vendor.id ?: throw IllegalStateException("Vendor id missing"),
                composedMaterial = composedMaterial,
                cleanColorName = cleanColorName,
                cleanColorHex = cleanColorHex,
                nozzleTemp = nozzleTemp,
                bedTemp = bedTemp,
                cleanRemainingWeight = cleanRemainingWeight,
                cleanEmptySpoolWeight = cleanEmptySpoolWeight
            )
        }

        return SpoolmanSaveResult(
            finalSpool = finalSpool,
            tagData = buildTagData(finalSpool, request, resolvedLotNr),
            resolvedLotNr = resolvedLotNr,
            actionMode = actionMode
        )
    }

    private suspend fun updateSpool(
        service: SpoolmanService,
        input: SpoolmanSaveInput,
        resolvedLotNr: String,
        vendorId: Int,
        composedMaterial: String,
        cleanColorName: String,
        cleanColorHex: String?,
        nozzleTemp: Int?,
        bedTemp: Int?,
        cleanRemainingWeight: Float?,
        cleanEmptySpoolWeight: Float?
    ): FilamentSpool {
        val request = input.request
        val spoolId = request.existingSpoolId
            ?: input.currentSpoolId?.toIntOrNull()
            ?: throw IllegalStateException("No spool id available for update")

        val currentFilamentId = input.selectedSpool?.filamentId
            ?: throw IllegalStateException("Current filament id missing")

        val usageCount = service.countSpoolsUsingFilament(currentFilamentId)

        val targetFilamentId = if (usageCount <= 1) {
            service.updateFilament(
                id = currentFilamentId,
                name = cleanColorName,
                material = composedMaterial,
                vendorId = vendorId,
                colorHex = cleanColorHex,
                nozzleTemp = nozzleTemp,
                bedTemp = bedTemp,
                spoolWeight = cleanEmptySpoolWeight
            ).id
        } else {
            service.createOrFindFilament(
                name = cleanColorName,
                material = composedMaterial,
                vendorId = vendorId,
                colorHex = cleanColorHex,
                nozzleTemp = nozzleTemp,
                bedTemp = bedTemp,
                spoolWeight = cleanEmptySpoolWeight
            ).id
        }

        service.updateSpool(
            id = spoolId,
            filamentId = targetFilamentId,
            lotNr = resolvedLotNr,
            location = request.location.trim().ifBlank { null },
            remainingWeight = cleanRemainingWeight ?: input.selectedSpool?.remainingWeight,
            comment = request.comment.trim().ifBlank { null }
        )

        return service.findFilamentBySpoolId(spoolId.toString())
            ?: throw IllegalStateException("Updated spool could not be reloaded")
    }

    private suspend fun createSpool(
        service: SpoolmanService,
        input: SpoolmanSaveInput,
        resolvedLotNr: String,
        vendorId: Int,
        composedMaterial: String,
        cleanColorName: String,
        cleanColorHex: String?,
        nozzleTemp: Int?,
        bedTemp: Int?,
        cleanRemainingWeight: Float?,
        cleanEmptySpoolWeight: Float?
    ): FilamentSpool {
        val request = input.request
        val filament = service.createOrFindFilament(
            name = cleanColorName,
            material = composedMaterial,
            vendorId = vendorId,
            colorHex = cleanColorHex,
            nozzleTemp = nozzleTemp,
            bedTemp = bedTemp,
            spoolWeight = cleanEmptySpoolWeight
        )

        val createdSpool = service.createSpool(
            filamentId = filament.id,
            lotNr = resolvedLotNr.ifBlank { null },
            location = request.location.trim().ifBlank { null },
            remainingWeight = cleanRemainingWeight ?: input.selectedSpool?.remainingWeight ?: filament.weight,
            comment = request.comment.trim().ifBlank { null }
        )

        return service.findFilamentBySpoolId(createdSpool.id?.toString() ?: "")
            ?: FilamentSpool.fromSpoolman(createdSpool.copy(filament = filament))
    }
}
