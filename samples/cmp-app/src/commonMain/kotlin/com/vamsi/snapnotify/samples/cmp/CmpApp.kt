package com.vamsi.snapnotify.samples.cmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vamsi.snapnotify.LocalSnapNotifyChannel
import com.vamsi.snapnotify.NotificationPlacement
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnapNotifyProvider
import com.vamsi.snapnotify.SnackbarHapticFeedback

/**
 * 100% shared Compose Multiplatform (CMM / CMP) UI in `commonMain`
 * shared across Android, iOS, Desktop, and Web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmpApp(platformName: String = "Multiplatform") {
    var placement by remember {
        mutableStateOf<NotificationPlacement>(NotificationPlacement.TopPill())
    }
    var count by remember { mutableIntStateOf(0) }
    var sheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(platformName) {
        SnapNotify.showSuccess("CMP Shared UI active on $platformName")
    }

    MaterialTheme {
        SnapNotifyProvider(placement = placement) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SnapNotify CMM/CMP ($platformName)") }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "100% Shared Compose Multiplatform UI",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Host platform: $platformName")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { placement = NotificationPlacement.BottomSnackbar },
                            modifier = Modifier.weight(1f)
                        ) { Text("Bottom") }
                        OutlinedButton(
                            onClick = { placement = NotificationPlacement.TopPill() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Top Pill") }
                        OutlinedButton(
                            onClick = { placement = NotificationPlacement.TopBanner() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Banner") }
                    }

                    Button(
                        onClick = { SnapNotify.showSuccess("Synced across CMP (#${++count})") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Shared Success")
                    }

                    Button(
                        onClick = { SnapNotify.showError("Cross-platform validation error (#${++count})") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Shared Error")
                    }

                    Button(
                        onClick = {
                            SnapNotify.show(
                                title = "Multiplatform Export Ready",
                                message = "Bundle exported on $platformName",
                                actionLabel = "Share",
                                onAction = { SnapNotify.showInfo("Shared from $platformName") },
                                showCloseButton = true,
                                hapticFeedback = SnackbarHapticFeedback.Success
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Rich Snackbar (Title + Close)")
                    }

                    Button(
                        onClick = {
                            SnapNotify.showUndoable(
                                message = "Draft archived on $platformName",
                                durationMillis = 5000L,
                                onAction = { SnapNotify.showInfo("Draft restored") },
                                onTimeout = { SnapNotify.showInfo("Draft permanently archived") }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Undoable Action with Countdown")
                    }

                    OutlinedButton(
                        onClick = { sheetOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Scoped Bottom Sheet Channel")
                    }
                }

                if (sheetOpen) {
                    ModalBottomSheet(onDismissRequest = { sheetOpen = false }) {
                        val sheetChannel = remember { SnapNotify.channel("cmp-sheet") }
                        DisposableEffect(sheetChannel) { onDispose { sheetChannel.clear() } }
                        SnapNotifyProvider(channel = sheetChannel) {
                            val local = LocalSnapNotifyChannel.current
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Scoped Sheet Channel (cmp-sheet)",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Button(
                                    onClick = { local.showSuccess("Scoped to CMP sheet!") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Trigger Sheet Snackbar")
                                }
                                Spacer(Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
