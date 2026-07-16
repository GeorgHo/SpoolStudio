package com.spoolstudio.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.spoolstudio.app.hardware.nfc.NfcHandler
import com.spoolstudio.app.ui.MainViewModel
import com.spoolstudio.app.ui.screens.MainScreenContent
import com.spoolstudio.app.ui.theme.SpoolStudioTheme

class MainActivity : ComponentActivity() {

    private lateinit var nfcHandler: NfcHandler
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.loadSpoolmanUrl(this)

        setupNfc()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        nfcHandler.enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcHandler.disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcHandler.handleIntent(intent)
    }

    private fun setupNfc() {
        nfcHandler = NfcHandler(this)
        nfcHandler.initialize()

        nfcHandler.onTagDetected = { data ->
            viewModel.handleNfcTagDetected(data)
        }

        nfcHandler.onStatusUpdate = { status, active ->
            viewModel.showSnackbarMessage(status, autoDismiss = !active)
        }
    }

    private fun setupUI() {
        enableEdgeToEdge()
        setContent {
            SpoolStudioTheme {
                MainScreenContent(
                    viewModel = viewModel,
                    onWriteTag = { data -> nfcHandler.writeToCurrentTag(data) },
                    onReadTag = { nfcHandler.enableReading() }
                )
            }
        }
    }
}
