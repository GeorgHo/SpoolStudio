package com.spoolstudio.app.domain.models

data class BambuTagDump(
    val tagId: String,
    val sectorCount: Int,
    val blocks: List<BambuSectorBlock>,
    val failedSectors: List<Int> = emptyList()
) {
    fun toFormattedHexDump(): String {
        if (blocks.isEmpty() && failedSectors.isEmpty()) {
            return "Keine Daten gelesen."
        }

        val grouped = blocks.groupBy { it.sectorIndex }
        val lines = mutableListOf<String>()

        for (sector in 0 until sectorCount) {
            lines += "=== Sektor $sector ==="

            if (failedSectors.contains(sector)) {
                lines += "AUTH FAILED"
                lines += ""
                continue
            }

            val sectorBlocks = grouped[sector].orEmpty()
                .sortedBy { it.blockIndexInSector }

            for (block in sectorBlocks) {
                lines += "Block %d (abs %d): %s".format(
                    block.blockIndexInSector,
                    block.absoluteBlockIndex,
                    block.hexData
                )
            }

            lines += ""
        }

        return lines.joinToString("\n")
    }
}