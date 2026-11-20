package com.vamsi.snapnotify.samples.kmm

import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnapNotifyConfig
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KmmOrderPresenterTest {

    @BeforeTest
    fun setup() {
        SnapNotify.configure(SnapNotifyConfig())
        SnapNotify.clearAll()
    }

    @AfterTest
    fun tearDown() {
        SnapNotify.clearAll()
        SnapNotify.configure(SnapNotifyConfig())
    }

    @Test
    fun `shared KMM presenter updates cart state and dispatches notifications`() {
        val presenter = KmmOrderPresenter()

        assertEquals(2, presenter.state.value.items.size)
        assertEquals(148, presenter.state.value.subtotalDollars)

        presenter.addItem("Test Mouse", 4900)
        assertEquals(3, presenter.state.value.items.size)
        assertEquals(197, presenter.state.value.subtotalDollars)

        presenter.removeLastItemWithUndo()
        assertEquals(2, presenter.state.value.items.size)

        presenter.submitOrder()
        assertEquals(1, presenter.state.value.ordersSubmitted)

        assertFalse(presenter.state.value.promoApplied)
        presenter.applyPromoCode("KMP20")
        assertTrue(presenter.state.value.promoApplied)

        presenter.checkoutChannel.clear()
    }
}
