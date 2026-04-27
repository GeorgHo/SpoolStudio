package com.spoolstudio.app.domain.models

data class Material(
    val name: String,
    val defaultMinTemp: Int,
    val defaultMaxTemp: Int,
    val defaultBedMinTemp: Int,
    val defaultBedMaxTemp: Int
)
