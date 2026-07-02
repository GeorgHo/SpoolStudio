package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode

class WriteOpenSpoolTagUseCase(
    private val serialize: (OpenSpoolData) -> String = { it.toJson() }
) {
    fun buildPayload(
        form: SpoolFormState,
        spoolMode: SpoolMode,
        selectedSpool: FilamentSpool?
    ): String? =
        form.buildOpenSpoolTagData(spoolMode, selectedSpool)?.let(serialize)
}
