package com.spoolstudio.app.ui

import com.spoolstudio.app.data.remote.spoolman.SpoolmanCatalog
import com.spoolstudio.app.data.remote.spoolman.SpoolmanService
import com.spoolstudio.app.domain.models.FilamentSpool

class SpoolmanCatalogRepository(
    private val serviceFactory: (String) -> SpoolmanService = ::SpoolmanService
) {
    suspend fun load(
        baseUrl: String,
        sortBy: String?,
        forceRefresh: Boolean = true
    ): SpoolmanCatalog {
        return serviceFactory(baseUrl).getCatalog(sortBy.ifNullOrEmpty(), forceRefresh = forceRefresh)
    }

    suspend fun findBySpoolId(baseUrl: String, spoolId: Int): FilamentSpool? {
        return serviceFactory(baseUrl).findFilamentBySpoolId(spoolId.toString())
    }
}

private fun String?.ifNullOrEmpty(): String? = this?.ifEmpty { null }
