package com.spoolstudio.app.hardware.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class NfcController(private val activity: Activity) {
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var pendingWriteData: String? = null
    private val nfcManager = NfcManager(activity.applicationContext)
    private var isReadingEnabled = false
    private val operationTimeoutHandler = Handler(Looper.getMainLooper())
    private var activeOperationId = 0
    
    var onTagDetected: ((String?) -> Unit)? = null
    var onStatusUpdate: ((String, Boolean) -> Unit)? = null

    fun initialize() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        
        when {
            nfcAdapter == null -> onStatusUpdate?.invoke("NFC not supported on this device", false)
            !nfcAdapter!!.isEnabled -> onStatusUpdate?.invoke("Please enable NFC in Settings", false)
        }

        pendingIntent = PendingIntent.getActivity(
            activity, 0, Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 
            PendingIntent.FLAG_MUTABLE
        )
    }

    fun writeToCurrentTag(data: String) {
        isReadingEnabled = false // Clear any pending read
        pendingWriteData = data
        onStatusUpdate?.invoke("Hold your phone near an NFC tag to write data", true)
        scheduleOperationTimeout(
            message = "No NFC tag found to write. Please try again."
        ) {
            pendingWriteData = null
        }
    }

    fun enableReading() {
        pendingWriteData = null
        isReadingEnabled = true
        onStatusUpdate?.invoke("Hold your phone near an NFC tag to read data", true)
        scheduleOperationTimeout(
            message = "No NFC tag found to read. Please try again."
        ) {
            isReadingEnabled = false
        }
    }

    fun enableForegroundDispatch() {
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun handleIntent(intent: Intent) {
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                val tag = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                }
                tag?.let { 
                    if (pendingWriteData != null) {
                        clearOperationTimeout()
                        // Write mode - write the pending data
                        if (nfcManager.writeTag(it, pendingWriteData!!)) {
                            onStatusUpdate?.invoke("✅ Successfully wrote to tag!", false)
                            pendingWriteData = null
                        } else {
                            onStatusUpdate?.invoke("❌ Write failed - try again", false)
                        }
                    } else if (isReadingEnabled) {
                        clearOperationTimeout()
                        // Read mode - read the tag
                        val data = nfcManager.readTag(it)
                        onTagDetected?.invoke(data)

                        if (data.isNullOrBlank()) {
                            onStatusUpdate?.invoke("❌ Tag detected but no data found", false)
                        } else {
                            val isHexDump =
                                data.contains("Bambu RFID Parsed") ||
                                        data.contains("=== Sektor") ||
                                        data.contains("Block 0 (abs")
                            if (isHexDump) {
                                onStatusUpdate?.invoke("✅ Bambu RFID dump gelesen", false)
                            } else {
                                onStatusUpdate?.invoke("✅ Tag read successfully!", false)
                            }
                        }

                        isReadingEnabled = false
                    } else {
                        onStatusUpdate?.invoke("Tag detected - press 'Read RFID Tag' to read data", false)
                    }
                }
            }
        }
    }

    private fun scheduleOperationTimeout(message: String, onTimeout: () -> Unit) {
        val operationId = ++activeOperationId
        operationTimeoutHandler.removeCallbacksAndMessages(null)
        operationTimeoutHandler.postDelayed(
            {
                if (operationId == activeOperationId) {
                    onTimeout()
                    activeOperationId++
                    onStatusUpdate?.invoke(message, false)
                }
            },
            NFC_OPERATION_TIMEOUT_MS
        )
    }

    private fun clearOperationTimeout() {
        activeOperationId++
        operationTimeoutHandler.removeCallbacksAndMessages(null)
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val NFC_OPERATION_TIMEOUT_MS = 10_000L
    }
}
