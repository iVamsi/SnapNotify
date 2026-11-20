package com.vamsi.snapnotify.samples.mac

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vamsi.snapnotify.NotificationPlacement
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnapNotifyProvider
import com.vamsi.snapnotify.SnackbarHapticFeedback
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

fun main(args: Array<String>) {
    val snapshotIndex = args.indexOf("--snapshot")
    if (snapshotIndex != -1 && snapshotIndex + 1 < args.size) {
        val outputFile = File(args[snapshotIndex + 1])
        renderMacSnapshot(outputFile)
        return
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SnapNotify 2.0 – macOS Desktop App",
            state = rememberWindowState(width = 760.dp, height = 560.dp)
        ) {
            MenuBar {
                Menu("Notifications") {
                    Item(
                        "Show macOS Success",
                        shortcut = KeyShortcut(Key.One, meta = true),
                        onClick = { SnapNotify.showSuccess("Saved to iCloud Drive (Cmd+1)") }
                    )
                    Item(
                        "Show macOS Error",
                        shortcut = KeyShortcut(Key.Two, meta = true),
                        onClick = { SnapNotify.showError("Disk permission denied (Cmd+2)") }
                    )
                    Item(
                        "Clear All",
                        shortcut = KeyShortcut(Key.K, meta = true),
                        onClick = { SnapNotify.clearAll() }
                    )
                }
            }
            MacDesktopSampleScreen()
        }
    }
}

private fun renderMacSnapshot(outputFile: File) {
    val scene = ImageComposeScene(width = 760, height = 560)
    try {
        scene.setContent {
            MacDesktopSampleScreen()
        }
        scene.render(0L)
        SnapNotify.show(
            title = "Xcode Build Succeeded",
            message = "macOS universal binary archived in 4.2s",
            actionLabel = "Reveal in Finder",
            onAction = {},
            showCloseButton = true,
            hapticFeedback = SnackbarHapticFeedback.Success
        )
        repeat(20) { step ->
            scene.render((step + 1) * 50_000_000L)
        }
        val image = scene.render(1_100_000_000L)
        val data = image.encodeToData(EncodedImageFormat.PNG)
            ?: error("Failed to encode macOS desktop snapshot")
        outputFile.parentFile?.mkdirs()
        outputFile.writeBytes(data.bytes)
    } finally {
        scene.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacDesktopSampleScreen() {
    var placement by remember {
        mutableStateOf<NotificationPlacement>(NotificationPlacement.TopPill())
    }
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        SnapNotify.showSuccess("macOS Desktop App Ready (Use Cmd+1 / Cmd+2 or click below)")
    }

    MaterialTheme {
        SnapNotifyProvider(placement = placement) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SnapNotify 2.0 – macOS Desktop Sample (.dmg)") }
                    )
                }
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "macOS Workspace Actions",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(
                                onClick = { SnapNotify.showSuccess("Exported macOS .dmg bundle (#${++count})") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Export macOS Bundle (Cmd+1)") }

                            Button(
                                onClick = {
                                    SnapNotify.show(
                                        title = "Spotlight Index Updated",
                                        message = "Indexed 1,240 project files on macOS",
                                        actionLabel = "Open",
                                        onAction = { SnapNotify.showInfo("Opened Finder") },
                                        showCloseButton = true
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Rich macOS Notification") }

                            Button(
                                onClick = {
                                    SnapNotify.showUndoable(
                                        message = "Moved folder to macOS Trash",
                                        durationMillis = 5000L,
                                        onAction = { SnapNotify.showInfo("Put Back from Trash") },
                                        onTimeout = { SnapNotify.showInfo("Emptied from Trash") }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Move to Trash (Undo Countdown)") }
                        }
                    }

                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Window Placement",
                                style = MaterialTheme.typography.titleMedium
                            )
                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.TopPill() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Top Pill Placement") }

                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.BottomSnackbar },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Bottom Dock Placement") }

                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.TopBanner() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Top Toolbar Banner") }
                        }
                    }
                }
            }
        }
    }
}
