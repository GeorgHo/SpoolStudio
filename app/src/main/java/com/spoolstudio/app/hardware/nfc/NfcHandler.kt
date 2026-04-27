package com.spoolstudio.app.hardware.nfc

import android.app.Activity

class NfcHandler(private val activity: Activity) {
    
    private lateinit var nfcController: NfcController
    
    var onTagDetected: ((String?) -> Unit)? = null
    var onStatusUpdate: ((String, Boolean) -> Unit)? = null

    fun initialize() {
        nfcController = NfcController(activity)
        nfcController.initialize()
        
        nfcController.onTagDetected = { data ->
            onTagDetected?.invoke(data)
        }
        
        nfcController.onStatusUpdate = { status, active ->
            onStatusUpdate?.invoke(status, active)
        }
    }

    fun enableForegroundDispatch() {
        nfcController.enableForegroundDispatch()
    }

    fun disableForegroundDispatch() {
        nfcController.disableForegroundDispatch()
    }

    fun handleIntent(intent: android.content.Intent) {
        nfcController.handleIntent(intent)
    }

    fun writeToCurrentTag(data: String) {
        nfcController.writeToCurrentTag(data)
    }

    fun enableReading() {
        nfcController.enableReading()
    }
}
