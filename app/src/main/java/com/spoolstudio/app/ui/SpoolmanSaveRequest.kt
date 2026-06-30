package com.spoolstudio.app.ui

data class SpoolmanSaveRequest(
    val material: String,
    val variant: String,
    val brand: String,
    val location: String,
    val colorHex: String?,
    val colorName: String,
    val minTemp: String,
    val maxTemp: String,
    val bedMinTemp: String,
    val bedMaxTemp: String,
    val lotNr: String,
    val comment: String,
    val remainingWeight: String,
    val existingSpoolId: Int?
)
