package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.After
import org.junit.Assert.*

class SnapNotifyProviderTest {

    @After
    fun tearDown() {
        // Reset provider registry state after each test
        ProviderRegistry.reset()
    }

    @Test
    fun testProviderRegistryBehavior() {
        // Reset state at beginning to ensure clean test
        ProviderRegistry.reset()

        // Test provider registry functionality without UI
        val isActive1 = ProviderRegistry.registerProvider()
        assertTrue("First provider should be active", isActive1)

        val isActive2 = ProviderRegistry.registerProvider()
        assertFalse("Second provider should not be active (nested)", isActive2)

        ProviderRegistry.unregisterProvider()
        ProviderRegistry.unregisterProvider() // Need to unregister both

        val isActive3 = ProviderRegistry.registerProvider()
        assertTrue("Provider should be active after unregistering all", isActive3)

        ProviderRegistry.unregisterProvider()
    }

    @Test
    fun testSnackbarDurationLogicWithoutUI() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Test that duration logic works properly in manager
        SnapNotify.show("Standard message", SnackbarDuration.Short)

        val message = snackbarManager.messages.value
        assertNotNull(message)
        assertEquals("Standard message", message?.text)
        assertEquals(SnackbarDuration.Short, message?.effectiveDuration?.getStandardDuration())
    }

    @Test
    fun testCustomDurationLogicWithoutUI() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        SnapNotify.show("Custom message", durationMillis = 5500)

        val message = snackbarManager.messages.value
        assertNotNull(message)
        assertEquals("Custom message", message?.text)
        assertEquals(5500L, message?.effectiveDuration?.getMilliseconds())
        assertTrue(message?.effectiveDuration is SnackbarDurationWrapper.Custom)
    }

    @Test
    fun testIndefiniteCustomDurationLogic() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        SnapNotify.show("Indefinite message", durationMillis = Long.MAX_VALUE)

        val message = snackbarManager.messages.value
        assertNotNull(message)
        assertEquals("Indefinite message", message?.text)
        assertTrue(message?.effectiveDuration?.isIndefinite() == true)
    }

    @Test
    fun testSnackbarStyleCreation() {
        // Test SnackbarStyle creation without UI dependencies
        val style = SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color.Blue,
            contentColor = androidx.compose.ui.graphics.Color.White
        )

        assertNotNull(style)
        assertEquals(androidx.compose.ui.graphics.Color.Blue, style.containerColor)
        assertEquals(androidx.compose.ui.graphics.Color.White, style.contentColor)
    }

    @Test
    fun testProviderRegistryReset() {
        // Reset state at beginning to ensure clean test
        ProviderRegistry.reset()

        // Test provider registry reset functionality
        ProviderRegistry.registerProvider()
        ProviderRegistry.registerProvider() // Second one should not be active

        ProviderRegistry.reset()

        // After reset, first provider should be active again
        val isActiveAfterReset = ProviderRegistry.registerProvider()
        assertTrue("Provider should be active after registry reset", isActiveAfterReset)

        ProviderRegistry.unregisterProvider()
    }
}