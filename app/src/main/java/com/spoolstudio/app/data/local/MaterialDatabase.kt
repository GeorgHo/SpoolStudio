package com.spoolstudio.app.data.local

import com.spoolstudio.app.domain.models.Material

object MaterialDatabase {
    val materials = listOf(
        Material("PLA", 190, 220, 40, 65),
        Material("ABS", 220, 260, 80, 110),
        Material("PETG", 220, 250, 60, 80),
        Material("TPU", 210, 230, 40, 60),
        Material("ASA", 240, 270, 100, 110),
        Material("PC", 270, 310, 80, 110),
        Material("Nylon", 240, 280, 60, 100),
        Material("PVA", 180, 210, 40, 60),
        Material("HIPS", 220, 260, 100, 110),
        Material("Other", 200, 220, 50, 70)
    )
    
    fun getMaterial(name: String): Material? = materials.find { it.name == name }
}
