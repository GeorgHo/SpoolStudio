package com.spoolstudio.app.data.local

import com.spoolstudio.app.domain.models.Material

object MaterialDatabase {
    val materials = listOf(
            Material("ABS", 240, 260, 90, 110),
            Material("ABS+", 240, 270, 90, 110),
            Material("APLA", 200, 220, 50, 60),
            Material("ASA", 240, 260, 90, 110),
            Material("ASA+", 245, 265, 90, 110),
            Material("BVOH", 190, 215, 45, 60),
            Material("CoPA", 250, 280, 70, 100),
            Material("CPE", 230, 260, 70, 90),
            Material("HIPS", 220, 250, 90, 110),
            Material("HT-PLA", 210, 240, 50, 70),
            Material("MABS", 230, 260, 80, 100),
            Material("PA6", 250, 280, 70, 100),
            Material("PA12", 240, 270, 60, 90),
            Material("PA612", 245, 275, 70, 90),
            Material("PA (Nylon)", 240, 280, 70, 100),
            Material("PAHT", 280, 320, 90, 120),
            Material("PBT", 240, 270, 80, 110),
            Material("PC", 260, 310, 100, 120),
            Material("PC-ABS", 260, 290, 100, 120),
            Material("PC-PBT", 260, 290, 100, 120),
            Material("PCL", 70, 120, 20, 45),
            Material("PCTG", 230, 260, 70, 90),
            Material("PE", 220, 260, 80, 110),
            Material("PEBA", 220, 260, 50, 80),
            Material("PEEK", 360, 420, 120, 160),
            Material("PEI", 340, 390, 120, 160),
            Material("PEKK", 340, 400, 120, 160),
            Material("PET", 220, 250, 60, 80),
            Material("PETG", 220, 250, 70, 90),
            Material("PETG+", 225, 260, 70, 90),
            Material("PHA", 190, 220, 45, 60),
            Material("PLA", 190, 220, 40, 65),
            Material("PLA+", 200, 230, 50, 70),
            Material("PMMA", 230, 260, 80, 110),
            Material("PP", 220, 250, 80, 110),
            Material("PPA", 280, 320, 90, 120),
            Material("PPS", 300, 340, 100, 140),
            Material("PSU", 340, 380, 120, 160),
            Material("PVA", 180, 210, 45, 60),
            Material("PVB", 190, 220, 50, 70),
            Material("PVDF", 240, 280, 90, 120),
            Material("SAN", 230, 260, 80, 110),
            Material("SBS", 220, 250, 70, 90),
            Material("SMP", 210, 240, 50, 70),
            Material("TPE", 210, 240, 40, 60),
            Material("TPR", 220, 250, 40, 60),
            Material("TPU", 210, 240, 40, 60)
    )
    
    fun getMaterial(name: String): Material? = materials.find { it.name == name }
}
