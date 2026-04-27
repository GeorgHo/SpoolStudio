package com.spoolstudio.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.spoolstudio.app.hardware.nfc.NfcHandler
import com.spoolstudio.app.ui.screens.MainScreenContent
import com.spoolstudio.app.ui.MainViewModel
import com.spoolstudio.app.ui.theme.SpoolStudioTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {
    
    private lateinit var nfcHandler: NfcHandler
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
        
        nfcHandler.onStatusUpdate = { status, _ ->
            viewModel.showSnackbarMessage(status)
        }
    }

    private fun setupUI() {
        enableEdgeToEdge()
        setContent {
            SpoolStudioTheme {
                var showLaunchIntro by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2400)
                    showLaunchIntro = false
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    MainScreenContent(
                        viewModel = viewModel,
                        onWriteTag = { data -> nfcHandler.writeToCurrentTag(data) },
                        onReadTag = { nfcHandler.enableReading() }
                    )

                    AnimatedVisibility(
                        visible = showLaunchIntro,
                        enter = fadeIn(animationSpec = tween(700)) +
                                scaleIn(
                                    initialScale = 0.90f,
                                    animationSpec = tween(
                                        durationMillis = 700,
                                        easing = FastOutSlowInEasing
                                    )
                                ),
                        exit = fadeOut(animationSpec = tween(700)) +
                                scaleOut(
                                    targetScale = 1.02f,
                                    animationSpec = tween(
                                        durationMillis = 700,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                    ) {
                        LaunchIntroOverlay(
                            onSkip = { showLaunchIntro = false }
                        )
                    }
                }
            }
        }
    }
    @Composable
    private fun LaunchIntroOverlay(onSkip: () -> Unit) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onSkip() },
            color = Color(0xFFF3E7DE)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SpoolStudioLogo(
                        color = Color(0xFFFFB300),
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 40.dp),
                        logoSize = 280.dp,
                        showTitle = false
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "© 2026 Spool Studio v1.2 by Hovi (unofficial)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A433F)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Based on SpoolPainter by ni4223",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A706A)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "With many thanks to ni4223, OpenSpool, Spoolman",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A706A)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "and the open-source community.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A706A)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tap to continue",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A918B),
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }
}
