package com.vamsi.snapnotify.samples.cmp

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SnapNotify CMM/CMP Sample",
        state = rememberWindowState(width = 480.dp, height = 800.dp)
    ) {
        CmpApp(platformName = "Desktop JVM")
    }
}
