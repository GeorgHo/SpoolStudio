package com.spoolstudio.app.ui

import com.spoolstudio.app.data.remote.spoolman.SpoolmanCatalog
import com.spoolstudio.app.data.remote.spoolman.SpoolmanService

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
}

private fun String?.ifNullOrEmpty(): String? = this?.ifEmpty { null }
