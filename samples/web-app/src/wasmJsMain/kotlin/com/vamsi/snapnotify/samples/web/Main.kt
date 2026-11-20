package com.vamsi.snapnotify.samples.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.ui.window.Dialog
import com.vamsi.snapnotify.LocalSnapNotifyChannel
import com.vamsi.snapnotify.NotificationPlacement
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnapNotifyProvider
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        WebAppSample()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAppSample() {
    var placement by remember {
        mutableStateOf<NotificationPlacement>(NotificationPlacement.TopPill())
    }
    var counter by remember { mutableIntStateOf(0) }
    var modalOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SnapNotify.showSuccess("SnapNotify 2.0 WebAssembly (wasmJs) App Loaded")
    }

    MaterialTheme {
        SnapNotifyProvider(placement = placement) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SnapNotify 2.0 – Web App (Kotlin/WasmJs)") }
                    )
                }
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Browser Notifications",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Button(
                                onClick = { SnapNotify.showSuccess("Saved to browser storage (#${++counter})") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Show Success Toast") }

                            Button(
                                onClick = { SnapNotify.showError("CORS / API request failed (#${++counter})") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Show Error Toast") }

                            Button(
                                onClick = {
                                    SnapNotify.showUndoable(
                                        message = "Row deleted from web table",
                                        durationMillis = 5000L,
                                        onAction = { SnapNotify.showInfo("Row restored") },
                                        onTimeout = { SnapNotify.showInfo("Row permanently deleted") }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Delete Row (Undo Countdown)") }
                        }
                    }

                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Web Placement & Scoped Channels",
                                style = MaterialTheme.typography.titleLarge
                            )
                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.TopPill() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Placement: Top Pill (Sonner-style)") }

                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.TopBanner() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Placement: Top Banner") }

                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.BottomSnackbar },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Placement: Bottom Snackbar") }

                            Button(
                                onClick = { modalOpen = true },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Open Scoped Web Modal") }
                        }
                    }
                }

                if (modalOpen) {
                    Dialog(onDismissRequest = { modalOpen = false }) {
                        Surface(shape = MaterialTheme.shapes.large) {
                            val channel = remember { SnapNotify.channel("web-modal") }
                            DisposableEffect(channel) { onDispose { channel.clear() } }
                            SnapNotifyProvider(channel = channel) {
                                val local = LocalSnapNotifyChannel.current
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Scoped Web Modal (web-modal)")
                                    Button(onClick = { local.showSuccess("Saved inside modal!") }) {
                                        Text("Trigger Modal Snackbar")
                                    }
                                    OutlinedButton(onClick = { modalOpen = false }) {
                                        Text("Close")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
