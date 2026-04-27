package com.spoolstudio.app.domain.models

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.round
import kotlin.math.pow

object BambuRfidParser {

    fun parse(
        uid: ByteArray,
        blocks: Map<Int, ByteArray>
    ): BambuParsedTag? {
        if (blocks.isEmpty()) return null

        val block1 = blocks[1]
        val block2 = blocks[2]
        val block4 = blocks[4]
        val block5 = blocks[5]
        val block6 = blocks[6]
        val block8 = blocks[8]
        val block10 = blocks[10]
        val block12 = blocks[12]
        val block13 = blocks[13]
        val block14 = blocks[14]
        val block16 = blocks[16]
        val block17 = blocks[17]

        val hasExtraColorInfo = block16?.let { it.size >= 2 && it[0] == 0x02.toByte() && it[1] == 0x00.toByte() } == true
        val colorCount = if (hasExtraColorInfo && block16 != null) leUInt16(block16, 2) else 1

        val primaryColor = block5?.let {
            if (it.size >= 4) "#" + it.copyOfRange(0, 3).toHexNoSeparator()
            else null
        }

        val secondaryColor = if (colorCount == 2 && block16 != null && block16.size >= 8) {
            "#" + block16.copyOfRange(4, 8).reversedArray().toHexNoSeparator()
        } else {
            null
        }

        return BambuParsedTag(
            uid = uid.toHexNoSeparator(),
            filamentType = block2?.toAsciiString(),
            detailedFilamentType = block4?.toAsciiString(),
            materialId = block1?.sliceSafe(8, 16)?.toAsciiString(),
            variantId = block1?.sliceSafe(0, 8)?.toAsciiString(),
            filamentColor = primaryColor,
            secondFilamentColor = secondaryColor,
            filamentColorCount = colorCount,
            spoolWeightGrams = block5?.let { leUInt16(it, 4) },
            filamentLengthMeters = block14?.let { leUInt16(it, 4) },
            filamentDiameterMm = block5?.let { leFloat(it, 8)?.roundTo(2) },
            spoolWidthMm = block10?.let { leUInt16(it, 4)?.div(100f)?.roundTo(2) },
            nozzleDiameterMm = block8?.let { leFloat(it, 12)?.roundTo(1) },
            dryingTempC = block6?.let { leUInt16(it, 0) },
            dryingTimeHours = block6?.let { leUInt16(it, 2) },
            bedTempType = block6?.let { leUInt16(it, 4) },
            bedTempC = block6?.let { leUInt16(it, 6) },
            maxHotendC = block6?.let { leUInt16(it, 8) },
            minHotendC = block6?.let { leUInt16(it, 10) },
            productionDate = block12?.let { parseAsciiDate(it) },
            unknown1 = block13?.toAsciiString(),
            unknown2Hex = block17?.sliceSafe(0, 2)?.toHexNoSeparator()
        )
    }

    private fun ByteArray.toAsciiString(): String =
        this
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .toString(Charsets.US_ASCII)
            .trim()

    private fun ByteArray.toHexNoSeparator(): String =
        joinToString("") { "%02X".format(it) }

    private fun ByteArray.sliceSafe(start: Int, endExclusive: Int): ByteArray {
        val safeStart = start.coerceAtLeast(0)
        val safeEnd = endExclusive.coerceAtMost(size)
        if (safeStart >= safeEnd) return byteArrayOf()
        return copyOfRange(safeStart, safeEnd)
    }

    private fun leUInt16(bytes: ByteArray, offset: Int): Int? {
        if (bytes.size < offset + 2) return null
        return ByteBuffer.wrap(bytes, offset, 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .short
            .toInt() and 0xFFFF
    }

    private fun leFloat(bytes: ByteArray, offset: Int): Float? {
        if (bytes.size < offset + 4) return null
        return ByteBuffer.wrap(bytes, offset, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .float
    }

    private fun parseAsciiDate(bytes: ByteArray): String? {
        val raw = bytes.toAsciiString()
        if (raw.isBlank()) return null

        return try {
            val digits = raw.filter { it.isDigit() }
            when {
                digits.length >= 8 -> {
                    val normalized = digits.take(8)
                    val date = LocalDate.parse(normalized, DateTimeFormatter.ofPattern("yyyyMMdd"))
                    date.toString()
                }
                else -> raw
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun Float.roundTo(decimals: Int): Float {
        val factor = 10.0.pow(decimals.toDouble()).toFloat()
        return round(this * factor) / factor
    }
}