package com.vamsi.snapnotify.samples.windows

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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        renderWindowsSnapshot(outputFile)
        return
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SnapNotify 2.0 – Windows Desktop App",
            state = rememberWindowState(width = 780.dp, height = 560.dp)
        ) {
            MenuBar {
                Menu("File & Notifications") {
                    Item(
                        "Trigger Windows Success (Ctrl+1)",
                        shortcut = KeyShortcut(Key.One, ctrl = true),
                        onClick = { SnapNotify.showSuccess("Saved to C:\\Users\\Documents (Ctrl+1)") }
                    )
                    Item(
                        "Trigger Windows Error (Ctrl+2)",
                        shortcut = KeyShortcut(Key.Two, ctrl = true),
                        onClick = { SnapNotify.showError("Windows Defender blocked unsigned binary (Ctrl+2)") }
                    )
                    Item(
                        "Clear Queue (Ctrl+K)",
                        shortcut = KeyShortcut(Key.K, ctrl = true),
                        onClick = { SnapNotify.clearAll() }
                    )
                }
            }
            WindowsDesktopSampleScreen()
        }
    }
}

private fun renderWindowsSnapshot(outputFile: File) {
    val scene = ImageComposeScene(width = 780, height = 560)
    try {
        scene.setContent {
            WindowsDesktopSampleScreen()
        }
        scene.render(0L)
        SnapNotify.show(
            title = "Windows Installer (.msi) Ready",
            message = "Saved to C:\\Builds\\SnapNotifyWindowsDemo-2.0.0.msi",
            actionLabel = "Open Folder",
            onAction = {},
            showCloseButton = true,
            hapticFeedback = SnackbarHapticFeedback.Success
        )
        repeat(20) { step ->
            scene.render((step + 1) * 50_000_000L)
        }
        val image = scene.render(1_100_000_000L)
        val data = image.encodeToData(EncodedImageFormat.PNG)
            ?: error("Failed to encode Windows desktop snapshot")
        outputFile.parentFile?.mkdirs()
        outputFile.writeBytes(data.bytes)
    } finally {
        scene.close()
    }
}

private val WindowsFluentDarkScheme = darkColorScheme(
    primary = Color(0xFF60CDFF),
    secondary = Color(0xFF9EA9FF),
    surface = Color(0xFF202020),
    background = Color(0xFF181818)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WindowsDesktopSampleScreen() {
    var placement by remember {
        mutableStateOf<NotificationPlacement>(NotificationPlacement.BottomSnackbar)
    }
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        SnapNotify.showSuccess("Windows Desktop App Ready (Use Ctrl+1 / Ctrl+2 or click below)")
    }

    MaterialTheme(colorScheme = WindowsFluentDarkScheme) {
        SnapNotifyProvider(placement = placement) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SnapNotify 2.0 – Windows Desktop Sample (.msi / .exe)") }
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
                                "Windows Desktop Actions",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(
                                onClick = {
                                    SnapNotify.showSuccess("Saved to C:\\Users\\Workspace (#${++count})")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Save to Local Disk (Ctrl+1)") }

                            Button(
                                onClick = {
                                    SnapNotify.show(
                                        title = "Windows Service Restarted",
                                        message = "Background telemetry worker is online",
                                        actionLabel = "Logs",
                                        onAction = { SnapNotify.showInfo("Viewing Event Viewer") },
                                        showCloseButton = true
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Rich Windows Notification") }

                            Button(
                                onClick = {
                                    SnapNotify.showUndoable(
                                        message = "Sent file to Recycle Bin",
                                        durationMillis = 5000L,
                                        onAction = { SnapNotify.showInfo("Restored from Recycle Bin") },
                                        onTimeout = { SnapNotify.showInfo("Deleted from Recycle Bin") }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Send to Recycle Bin (Undo)") }
                        }
                    }

                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Windows Taskbar / Window Placement",
                                style = MaterialTheme.typography.titleMedium
                            )
                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.BottomSnackbar },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Bottom Taskbar Placement") }

                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.TopPill() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Top Center Pill") }

                            OutlinedButton(
                                onClick = { placement = NotificationPlacement.TopBanner() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Top Window Banner") }
                        }
                    }
                }
            }
        }
    }
}
