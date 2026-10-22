package com.vamsi.snapnotify.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vamsi.snapnotify.LocalSnapNotifyChannel
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnapNotifyProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScopedChannelDemo() {
    var sheetOpen by remember { mutableStateOf(false) }
    var dialogOpen by remember { mutableStateOf(false) }
    Button(onClick = { sheetOpen = true }) { Text("Open scoped bottom sheet") }
    Button(onClick = { dialogOpen = true }) { Text("Open scoped dialog") }

    if (sheetOpen) {
        ModalBottomSheet(onDismissRequest = { sheetOpen = false }) {
            ScopedContent("demo-sheet")
        }
    }
    if (dialogOpen) {
        Dialog(onDismissRequest = { dialogOpen = false }) {
            Surface { ScopedContent("demo-dialog") }
        }
    }
}

@Composable
private fun ScopedContent(id: String) {
    val channel = remember(id) { SnapNotify.channel(id) }
    DisposableEffect(channel) { onDispose { channel.clear() } }
    SnapNotifyProvider(channel = channel) {
        val localChannel = LocalSnapNotifyChannel.current
        Column(Modifier.padding(16.dp)) {
            Text("Independent channel: $id")
            Button(onClick = { localChannel.showSuccess("Saved in $id") }) { Text("Scoped success") }
            Button(onClick = { localChannel.showUndoable("Removed in $id", onAction = {}) }) { Text("Scoped Undo") }
            Button(onClick = { SnapNotify.show("Message in the root host") }) { Text("Send to root host") }
        }
    }
}
