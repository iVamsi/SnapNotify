package com.vamsi.snapnotify.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vamsi.snapnotify.NotificationPlacement
import com.vamsi.snapnotify.demo.ui.theme.SnapNotifyTheme
import com.vamsi.snapnotify.SnapNotifyProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapNotifyTheme {
                var placement by remember {
                    mutableStateOf<NotificationPlacement>(NotificationPlacement.BottomSnackbar)
                }
                SnapNotifyProvider(placement = placement) {
                    DemoScreen(
                        modifier = Modifier.fillMaxSize(),
                        onPlacementSelected = { placement = it },
                    )
                }
            }
        }
    }
}
