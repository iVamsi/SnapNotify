package com.vamsi.snapnotify.samples.kmm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.vamsi.snapnotify.SnapNotifyProvider
import platform.UIKit.UIViewController

/**
 * iOS bridge exposing the shared [KmmOrderPresenter] and a [SnapNotifyProvider] host
 * to the native iOS SwiftUI front-end (`SnapNotifyKmmApp.swift`).
 */
object KmmIosBridge {
    val presenter = KmmOrderPresenter()

    fun itemCount(): Int = presenter.state.value.items.size
    fun subtotalDollars(): Int = presenter.state.value.subtotalDollars
    fun ordersSubmitted(): Int = presenter.state.value.ordersSubmitted

    fun addHeadphones() {
        presenter.addItem("Noise-Cancelling Headphones", 24900)
    }

    fun removeLastItemWithUndo() {
        presenter.removeLastItemWithUndo()
    }

    fun submitOrder() {
        presenter.submitOrder()
    }

    fun applyValidPromo() {
        presenter.applyPromoCode("KMP20")
    }

    fun applyInvalidPromo() {
        presenter.applyPromoCode("INVALID")
    }

    fun simulateNetworkOutage() {
        presenter.simulateNetworkOutage()
    }

    fun createGlobalHostController(): UIViewController = ComposeUIViewController {
        MaterialTheme {
            SnapNotifyProvider {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
