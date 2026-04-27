package com.spoolstudio.app.hardware.nfc

import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import java.nio.charset.Charset
import android.nfc.tech.MifareClassic
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import com.spoolstudio.app.domain.models.BambuRfidParser

class NfcManager(private val context: android.content.Context) {
    private fun ByteArray.toHexString(separator: String = " "): String =
        joinToString(separator) { "%02X".format(it) }

    private fun loadBambuMasterKey(): ByteArray? {
        val prefs = context.getSharedPreferences("spoolstudio_prefs", android.content.Context.MODE_PRIVATE)
        val raw = prefs.getString("bambu_master_key", "")?.trim()?.uppercase() ?: ""

        if (!raw.matches(Regex("^[0-9A-F]{32}$"))) return null

        return raw
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun deriveBambuSectorKeys(uid: ByteArray): List<ByteArray> {
        val masterKey = loadBambuMasterKey() ?: return emptyList()

        val info = byteArrayOf(
            'R'.code.toByte(),
            'F'.code.toByte(),
            'I'.code.toByte(),
            'D'.code.toByte(),
            '-'.code.toByte(),
            'A'.code.toByte(),
            0x00
        )

        val generator = HKDFBytesGenerator(SHA256Digest())
        generator.init(HKDFParameters(uid, masterKey, info))

        val output = ByteArray(16 * 6)
        generator.generateBytes(output, 0, output.size)

        return output.asList()
            .chunked(6)
            .map { chunk -> chunk.toByteArray() }
    }

    private val bambuRelevantSectors = 0..4
    // true = auch Trailer-Blöcke (Block 3 je Sektor) mit dumpen
    // false = nur Nutzdatenblöcke
    private val includeTrailerBlocks = true

    fun readTag(tag: Tag): String? {
        // 1) Erst wie bisher NDEF versuchen
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val ndefMessage = ndef.ndefMessage
                ndef.close()

                val payload = ndefMessage?.records?.firstOrNull()?.let { record ->
                    String(record.payload, Charset.forName("UTF-8"))
                }

                if (!payload.isNullOrBlank()) {
                    return payload
                }
            }
        } catch (_: Exception) {
            // absichtlich weiter zum MIFARE-Classic-Fallback
        }

        // 2) Bambu / MIFARE Classic Fallback
        return try {
            val mifare = MifareClassic.get(tag) ?: return null
            mifare.connect()

            val uid = tag.id
            val sectorKeys = deriveBambuSectorKeys(uid)
            val sectorCount = mifare.sectorCount
            val lines = mutableListOf<String>()
            val parsedBlocks = mutableMapOf<Int, ByteArray>()

            lines += "MIFARE Classic"
            lines += "Tag ID: ${uid.toHexString(separator = "")}"
            lines += "Sectors: $sectorCount"
            lines += "Reading sectors: ${bambuRelevantSectors.first}..${bambuRelevantSectors.last}"
            lines += "Trailer blocks: ${if (includeTrailerBlocks) "included" else "skipped"}"
            lines += ""

            for (sectorIndex in bambuRelevantSectors) {
                if (sectorIndex >= sectorCount) continue
                lines += "=== Sektor $sectorIndex ==="

                val keyA = sectorKeys.getOrNull(sectorIndex)
                if (keyA == null) {
                    lines += "NO KEY"
                    lines += ""
                    continue
                }

                val authOk = try {
                    mifare.authenticateSectorWithKeyA(sectorIndex, keyA)
                } catch (_: Exception) {
                    false
                }

                if (!authOk) {
                    lines += "AUTH FAILED"
                    lines += "KeyA: ${keyA.toHexString(separator = "")}"
                    lines += ""
                    continue
                }

                val blockCount = mifare.getBlockCountInSector(sectorIndex)
                val startBlock = mifare.sectorToBlock(sectorIndex)

                lines += "KeyA: ${keyA.toHexString(separator = "")}"

                for (blockIndexInSector in 0 until blockCount) {
                    val isTrailerBlock = blockIndexInSector == blockCount - 1
                    if (!includeTrailerBlocks && isTrailerBlock) continue

                    val absoluteBlock = startBlock + blockIndexInSector

                    val blockData = try {
                        mifare.readBlock(absoluteBlock)
                    } catch (_: Exception) {
                        null
                    }

                    val suffix = if (isTrailerBlock) " [TRAILER]" else ""

                    if (blockData != null) {
                        parsedBlocks[absoluteBlock] = blockData
                        lines += "Block $blockIndexInSector (abs $absoluteBlock)$suffix: ${blockData.toHexString()}"
                    } else {
                        lines += "Block $blockIndexInSector (abs $absoluteBlock)$suffix: READ ERROR"
                    }
                }

                lines += ""
            }

            mifare.close()

            val parsed = BambuRfidParser.parse(
                uid = uid,
                blocks = parsedBlocks
            )

            parsed?.toDisplayString() ?: lines.joinToString("\n")
        } catch (_: Exception) {
            null
        }
    }
    
    fun writeTag(tag: Tag, data: String): Boolean {
        return try {
            val ndef = Ndef.get(tag) ?: return false
            ndef.connect()
            
            val record = NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "application/json".toByteArray(),
                ByteArray(0),
                data.toByteArray(Charset.forName("UTF-8"))
            )
            val message = NdefMessage(arrayOf(record))
            
            ndef.writeNdefMessage(message)
            ndef.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
