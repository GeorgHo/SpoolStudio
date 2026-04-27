package com.spoolstudio.app.domain.models

data class BambuSectorBlock(
    val sectorIndex: Int,
    val blockIndexInSector: Int,
    val absoluteBlockIndex: Int,
    val hexData: String,
    val isTrailerBlock: Boolean = false
)