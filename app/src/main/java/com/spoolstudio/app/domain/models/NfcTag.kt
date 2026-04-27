package com.spoolstudio.app.domain.models

data class NfcTag(
    val id: String,
    val data: String? = null,
    val isWritable: Boolean = true
)
