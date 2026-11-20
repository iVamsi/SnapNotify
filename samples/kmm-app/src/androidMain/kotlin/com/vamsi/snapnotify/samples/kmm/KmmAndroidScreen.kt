package com.vamsi.snapnotify.samples.kmm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vamsi.snapnotify.SnapNotifyProvider

/**
 * Native Android Jetpack Compose UI consuming the shared [KmmOrderPresenter].
 */
@Composable
fun KmmAndroidScreen(
    presenter: KmmOrderPresenter = remember { KmmOrderPresenter() }
) {
    val state by presenter.state.collectAsState()

    MaterialTheme {
        SnapNotifyProvider {
            Scaffold { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SnapNotify KMM App (Android Native UI)",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Items in Cart: ${state.items.size}")
                            Text("Subtotal: $${state.subtotalDollars}")
                            Text("Orders Submitted: ${state.ordersSubmitted}")
                        }
                    }

                    Button(
                        onClick = { presenter.addItem("Noise-Cancelling Headphones", 24900) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Item (Shared KMM Logic)")
                    }

                    Button(
                        onClick = { presenter.removeLastItemWithUndo() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove Last Item with Undo")
                    }

                    Button(
                        onClick = { presenter.submitOrder() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Order (Rich Notification)")
                    }

                    OutlinedButton(
                        onClick = { presenter.simulateNetworkOutage() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Urgent Network Outage")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scoped Checkout Channel:", style = MaterialTheme.typography.titleSmall)
                    SnapNotifyProvider(channel = presenter.checkoutChannel) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { presenter.applyPromoCode("KMP20") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply Valid Promo (KMP20)")
                            }
                            OutlinedButton(
                                onClick = { presenter.applyPromoCode("EXPIRED") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply Invalid Promo")
                            }
                        }
                    }
                }
            }
        }
    }
}
