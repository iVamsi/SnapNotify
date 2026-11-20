package com.vamsi.snapnotify.samples.kmm

import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnapNotifyChannel
import com.vamsi.snapnotify.SnackbarHapticFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class KmmCartItem(
    val id: String,
    val name: String,
    val priceCents: Int,
)

data class KmmOrderState(
    val items: List<KmmCartItem> = listOf(
        KmmCartItem("1", "Wireless Mechanical Keyboard", 12900),
        KmmCartItem("2", "USB-C Braided Cable", 1900),
    ),
    val promoApplied: Boolean = false,
    val ordersSubmitted: Int = 0,
) {
    val subtotalDollars: Int
        get() = items.sumOf { it.priceCents } / 100
}

/**
 * Shared KMM presentation/domain logic in `commonMain`.
 *
 * Neither Android `Context` nor iOS `UIViewController` is passed into this class —
 * it triggers [SnapNotify] directly from shared business logic.
 */
class KmmOrderPresenter {
    private val _state = MutableStateFlow(KmmOrderState())
    val state: StateFlow<KmmOrderState> = _state.asStateFlow()

    val checkoutChannel: SnapNotifyChannel = SnapNotify.channel("kmm-checkout")

    fun addItem(name: String, priceCents: Int) {
        val nextId = (_state.value.items.size + 1).toString()
        _state.update { current ->
            current.copy(items = current.items + KmmCartItem(nextId, name, priceCents))
        }
        SnapNotify.showSuccess("Added \"$name\" to cart ($${priceCents / 100})")
    }

    fun removeLastItemWithUndo() {
        val currentItems = _state.value.items
        if (currentItems.isEmpty()) {
            SnapNotify.showWarning("Cart is already empty")
            return
        }
        val removed = currentItems.last()
        _state.update { it.copy(items = currentItems.dropLast(1)) }

        SnapNotify.showUndoable(
            message = "Removed ${removed.name}",
            durationMillis = 5000L,
            actionLabel = "Undo",
            onAction = {
                _state.update { it.copy(items = it.items + removed) }
                SnapNotify.showInfo("Restored ${removed.name}")
            },
            onTimeout = {
                // Item remains removed after undo window expires
            }
        )
    }

    fun applyPromoCode(code: String) {
        if (code.trim().equals("KMP20", ignoreCase = true)) {
            _state.update { it.copy(promoApplied = true) }
            checkoutChannel.showSuccess("Promo KMP20 applied on checkout channel!")
        } else {
            checkoutChannel.showError("Invalid promo code \"$code\"")
        }
    }

    fun submitOrder() {
        val current = _state.value
        if (current.items.isEmpty()) {
            SnapNotify.showError("Cannot submit an empty cart")
            return
        }
        _state.update { it.copy(ordersSubmitted = it.ordersSubmitted + 1) }
        SnapNotify.show(
            title = "Order #${_state.value.ordersSubmitted} Confirmed",
            message = "${current.items.size} items ($${current.subtotalDollars}) dispatched via KMM shared presenter",
            actionLabel = "Track",
            onAction = { SnapNotify.showInfo("Tracking Order #${_state.value.ordersSubmitted}") },
            showCloseButton = true,
            hapticFeedback = SnackbarHapticFeedback.Success,
        )
    }

    fun simulateNetworkOutage() {
        SnapNotify.showUrgent(
            message = "URGENT: Payment gateway connection lost — retrying",
            actionLabel = "Retry",
            onAction = { SnapNotify.showInfo("Reconnecting to payment gateway...") }
        )
    }
}
