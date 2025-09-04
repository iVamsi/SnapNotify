package com.vamsi.snapnotify.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnackbarStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen() {
    var counter by remember { mutableIntStateOf(0) }
    
    // Pre-define styles to avoid @Composable issues in onClick
    val purpleStyle = SnackbarStyle(
        containerColor = Color(0xFF6A1B9A),
        contentColor = Color.White,
        actionColor = Color(0xFFE1BEE7),
        shape = RoundedCornerShape(16.dp),
        elevation = 12.dp
    )
    
    val brandStyle = SnackbarStyle(
        containerColor = Color(0xFF2E7D32),
        contentColor = Color.White,
        actionColor = Color(0xFF81C784),
        shape = RoundedCornerShape(8.dp),
        messageTextStyle = MaterialTheme.typography.bodyLarge,
        actionTextStyle = MaterialTheme.typography.labelLarge
    )
    
    val darkStyle = SnackbarStyle(
        containerColor = Color(0xFF212121),
        contentColor = Color(0xFFE0E0E0),
        actionColor = Color(0xFF64B5F6),
        shape = RoundedCornerShape(4.dp)
    )

    // Scroll behavior for collapsible top bar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("SnapNotify Demo") },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0.dp, 0.dp)
            )
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Tap buttons to see SnapNotify in action!")
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                SnapNotify.show("Simple message ${++counter}")
            }) {
                Text("Show Simple Message")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                SnapNotify.show(
                    message = "Long duration message ${++counter}",
                    duration = SnackbarDuration.Long
                )
            }) {
                Text("Show Long Message")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                SnapNotify.show(
                    message = "Message with action ${++counter}",
                    actionLabel = "Undo",
                    onAction = {
                        SnapNotify.show("Undo action performed!")
                        counter--
                    }
                )
            }) {
                Text("Show Message with Action")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Show multiple messages quickly to test queueing
                repeat(3) { index ->
                    SnapNotify.show("Queued message ${index + 1}")
                }
            }) {
                Text("Test Message Queue")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                SnapNotify.clearAll()
            }) {
                Text("Clear All Messages")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                SnapNotify.show(
                    message = "This won't disappear automatically",
                    duration = SnackbarDuration.Indefinite,
                    actionLabel = "Dismiss",
                    onAction = {}
                )
            }) {
                Text("Show Indefinite Message")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Styling Demo Buttons
            Button(onClick = {
                SnapNotify.showSuccess("Operation completed successfully! ${++counter}")
            }) {
                Text("Show Success Message")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                SnapNotify.showError("Something went wrong! ${++counter}")
            }) {
                Text("Show Error Message")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                SnapNotify.showWarning("Please check your input! ${++counter}")
            }) {
                Text("Show Warning Message")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                SnapNotify.showInfo("Here's some information! ${++counter}")
            }) {
                Text("Show Info Message")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                SnapNotify.showSuccess(
                    message = "File saved successfully!",
                    actionLabel = "View",
                    onAction = {
                        SnapNotify.show("View action performed!")
                    }
                )
            }) {
                Text("Success with Action")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Custom Styling Demo Buttons
            Button(onClick = {
                SnapNotify.showStyled("Custom purple theme! ${++counter}", purpleStyle)
            }) {
                Text("Custom Purple Style")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                SnapNotify.showStyled(
                    message = "Custom styled with action!",
                    style = brandStyle,
                    actionLabel = "Cool!",
                    onAction = {
                        SnapNotify.show("Custom action performed!")
                    }
                )
            }) {
                Text("Custom Style with Action")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                SnapNotify.showStyled("Dark theme message! ${++counter}", darkStyle)
            }) {
                Text("Dark Custom Style")
            }
        }
    }
}
