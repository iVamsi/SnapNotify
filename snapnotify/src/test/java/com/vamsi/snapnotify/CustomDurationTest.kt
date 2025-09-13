package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class CustomDurationTest {

    @Test
    fun testDurationWrapperCreation() {
        val standardShort = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Short)
        val standardLong = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Long)
        val standardIndefinite = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Indefinite)

        val custom5Seconds = SnackbarDurationWrapper.fromMillis(5000)
        val custom2Point5Seconds = SnackbarDurationWrapper.fromSeconds(2.5)

        assertTrue(standardShort is SnackbarDurationWrapper.Standard)
        assertTrue(standardLong is SnackbarDurationWrapper.Standard)
        assertTrue(standardIndefinite is SnackbarDurationWrapper.Standard)
        assertTrue(custom5Seconds is SnackbarDurationWrapper.Custom)
        assertTrue(custom2Point5Seconds is SnackbarDurationWrapper.Custom)

        assertEquals(SnackbarDuration.Short, standardShort.getStandardDuration())
        assertEquals(SnackbarDuration.Long, standardLong.getStandardDuration())
        assertEquals(SnackbarDuration.Indefinite, standardIndefinite.getStandardDuration())
        assertNull(custom5Seconds.getStandardDuration())
        assertNull(custom2Point5Seconds.getStandardDuration())
    }

    @Test
    fun testDurationWrapperMilliseconds() {
        val shortDuration = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Short)
        val longDuration = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Long)
        val indefiniteDuration = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Indefinite)
        val custom3Seconds = SnackbarDurationWrapper.fromMillis(3000)
        val custom1Point5Seconds = SnackbarDurationWrapper.fromSeconds(1.5)

        assertEquals(4000L, shortDuration.getMilliseconds())
        assertEquals(10000L, longDuration.getMilliseconds())
        assertEquals(Long.MAX_VALUE, indefiniteDuration.getMilliseconds())
        assertEquals(3000L, custom3Seconds.getMilliseconds())
        assertEquals(1500L, custom1Point5Seconds.getMilliseconds())
    }

    @Test
    fun testIndefiniteDurationDetection() {
        val shortDuration = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Short)
        val indefiniteDuration = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Indefinite)
        val customFiniteDuration = SnackbarDurationWrapper.fromMillis(5000)
        val customInfiniteDuration = SnackbarDurationWrapper.fromMillis(Long.MAX_VALUE)

        assertFalse(shortDuration.isIndefinite())
        assertTrue(indefiniteDuration.isIndefinite())
        assertFalse(customFiniteDuration.isIndefinite())
        assertTrue(customInfiniteDuration.isIndefinite())
    }

    @Test
    fun testInvalidDurationThrowsException() {
        try {
            SnackbarDurationWrapper.fromMillis(0)
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            SnackbarDurationWrapper.fromMillis(-1000)
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            SnackbarDurationWrapper.fromSeconds(0.0)
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            SnackbarDurationWrapper.fromSeconds(-1.5)
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun testSnackbarManagerCustomDuration() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        snackbarManager.showWithCustomDuration("Test message", 3000)

        val currentMessage = snackbarManager.messages.value
        assertNotNull(currentMessage)
        assertEquals("Test message", currentMessage?.text)

        val effectiveDuration = currentMessage?.effectiveDuration
        assertNotNull(effectiveDuration)
        assertTrue(effectiveDuration is SnackbarDurationWrapper.Custom)
        assertEquals(3000L, effectiveDuration?.getMilliseconds())
    }

    @Test
    fun testSnackbarManagerCustomDurationWithAction() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        var actionExecuted = false
        snackbarManager.showWithCustomDuration(
            "Test message with action",
            2500,
            "Action"
        ) {
            actionExecuted = true
        }

        val currentMessage = snackbarManager.messages.value
        assertNotNull(currentMessage)
        assertEquals("Test message with action", currentMessage?.text)
        assertEquals("Action", currentMessage?.actionLabel)
        assertNotNull(currentMessage?.onAction)

        // Execute the action
        currentMessage?.onAction?.invoke()
        assertTrue(actionExecuted)

        val effectiveDuration = currentMessage?.effectiveDuration
        assertNotNull(effectiveDuration)
        assertEquals(2500L, effectiveDuration?.getMilliseconds())
    }

    @Test
    fun testSnapNotifyPublicAPICustomDuration() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Test basic custom duration
        SnapNotify.show("Custom duration message", durationMillis = 7000)

        val currentMessage = snackbarManager.messages.value
        assertNotNull(currentMessage)
        assertEquals("Custom duration message", currentMessage?.text)
        assertEquals(7000L, currentMessage?.effectiveDuration?.getMilliseconds())

        // Clear and test with action
        snackbarManager.clearAll()
        var actionTriggered = false
        SnapNotify.show("Action message", "Click", {
            actionTriggered = true
        }, durationMillis = 1500)

        val messageWithAction = snackbarManager.messages.value
        assertNotNull(messageWithAction)
        assertEquals("Action message", messageWithAction?.text)
        assertEquals("Click", messageWithAction?.actionLabel)
        assertEquals(1500L, messageWithAction?.effectiveDuration?.getMilliseconds())

        // Execute action
        messageWithAction?.onAction?.invoke()
        assertTrue(actionTriggered)
    }

    @Test
    fun testSnapNotifyThemedCustomDuration() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Test success theme with custom duration
        SnapNotify.showSuccess("Success message", durationMillis = 6000)

        val successMessage = snackbarManager.messages.value
        assertNotNull(successMessage)
        assertEquals("Success message", successMessage?.text)
        assertEquals(6000L, successMessage?.effectiveDuration?.getMilliseconds())
        assertNotNull(successMessage?.style)

        // Test error theme with custom duration and action
        snackbarManager.clearAll()
        var retryTriggered = false
        SnapNotify.showError("Error occurred", "Retry", {
            retryTriggered = true
        }, durationMillis = 8000)

        val errorMessage = snackbarManager.messages.value
        assertNotNull(errorMessage)
        assertEquals("Error occurred", errorMessage?.text)
        assertEquals("Retry", errorMessage?.actionLabel)
        assertEquals(8000L, errorMessage?.effectiveDuration?.getMilliseconds())
        assertNotNull(errorMessage?.style)

        // Execute retry action
        errorMessage?.onAction?.invoke()
        assertTrue(retryTriggered)

        // Test warning theme
        snackbarManager.clearAll()
        SnapNotify.showWarning("Warning message", durationMillis = 4500)

        val warningMessage = snackbarManager.messages.value
        assertNotNull(warningMessage)
        assertEquals("Warning message", warningMessage?.text)
        assertEquals(4500L, warningMessage?.effectiveDuration?.getMilliseconds())

        // Test info theme
        snackbarManager.clearAll()
        SnapNotify.showInfo("Info message", durationMillis = 3500)

        val infoMessage = snackbarManager.messages.value
        assertNotNull(infoMessage)
        assertEquals("Info message", infoMessage?.text)
        assertEquals(3500L, infoMessage?.effectiveDuration?.getMilliseconds())
    }

    @Test
    fun testCustomDurationWithStyling() = runTest {
        val customStyle = SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color.Blue,
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color.Yellow
        )

        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        SnapNotify.showStyled("Styled message", customStyle, durationMillis = 5500)

        val styledMessage = snackbarManager.messages.value
        assertNotNull(styledMessage)
        assertEquals("Styled message", styledMessage?.text)
        assertEquals(5500L, styledMessage?.effectiveDuration?.getMilliseconds())
        assertEquals(customStyle, styledMessage?.style)

        // Test styled with action
        snackbarManager.clearAll()
        var styledActionTriggered = false
        SnapNotify.showStyled("Styled with action", customStyle, "Custom Action", {
            styledActionTriggered = true
        }, durationMillis = 2200)

        val styledWithAction = snackbarManager.messages.value
        assertNotNull(styledWithAction)
        assertEquals("Styled with action", styledWithAction?.text)
        assertEquals("Custom Action", styledWithAction?.actionLabel)
        assertEquals(2200L, styledWithAction?.effectiveDuration?.getMilliseconds())
        assertEquals(customStyle, styledWithAction?.style)

        styledWithAction?.onAction?.invoke()
        assertTrue(styledActionTriggered)
    }

    @Test
    fun testEdgeCases() {
        // Test boundary values
        val minDuration = SnackbarDurationWrapper.fromMillis(1)
        assertEquals(1L, minDuration.getMilliseconds())
        assertFalse(minDuration.isIndefinite())

        val maxDuration = SnackbarDurationWrapper.fromMillis(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, maxDuration.getMilliseconds())
        assertTrue(maxDuration.isIndefinite())

        val smallFraction = SnackbarDurationWrapper.fromSeconds(0.001)
        assertEquals(1L, smallFraction.getMilliseconds())

        // Test all standard durations conversion
        val allStandardDurations = listOf(
            SnackbarDuration.Short to 4000L,
            SnackbarDuration.Long to 10000L,
            SnackbarDuration.Indefinite to Long.MAX_VALUE
        )

        allStandardDurations.forEach { (duration, expectedMillis) ->
            val wrapper = SnackbarDurationWrapper.fromStandard(duration)
            assertEquals(expectedMillis, wrapper.getMilliseconds())
            assertEquals(duration, wrapper.getStandardDuration())
        }
    }

    @Test
    fun testSnackbarMessageEffectiveDurationProperty() {
        // Test that effectiveDuration property works correctly
        val message1 = com.vamsi.snapnotify.core.SnackbarMessage(
            text = "Test",
            duration = SnackbarDuration.Long
        )
        assertEquals(SnackbarDuration.Long, message1.effectiveDuration.getStandardDuration())
        assertEquals(10000L, message1.effectiveDuration.getMilliseconds())

        val customDuration = SnackbarDurationWrapper.fromMillis(7500)
        val message2 = com.vamsi.snapnotify.core.SnackbarMessage(
            text = "Test",
            duration = SnackbarDuration.Short, // This should be overridden
            customDuration = customDuration
        )
        assertNull(message2.effectiveDuration.getStandardDuration())
        assertEquals(7500L, message2.effectiveDuration.getMilliseconds())
    }

    @Test
    fun testAllSnapNotifyMethodsWithCustomDuration() = runTest {
        val snackbarManager = SnackbarManager.getInstance()

        // Test all method overloads work with custom duration
        snackbarManager.clearAll()
        SnapNotify.show("Test", durationMillis = 1000)
        assertEquals(1000L, snackbarManager.messages.value?.effectiveDuration?.getMilliseconds())

        snackbarManager.clearAll()
        SnapNotify.show("Test", "Action", {}, durationMillis = 2000)
        assertEquals(2000L, snackbarManager.messages.value?.effectiveDuration?.getMilliseconds())

        val style = SnackbarStyle()
        snackbarManager.clearAll()
        SnapNotify.showStyled("Test", style, durationMillis = 3000)
        assertEquals(3000L, snackbarManager.messages.value?.effectiveDuration?.getMilliseconds())

        snackbarManager.clearAll()
        SnapNotify.showStyled("Test", style, "Action", {}, durationMillis = 4000)
        assertEquals(4000L, snackbarManager.messages.value?.effectiveDuration?.getMilliseconds())
    }

    @Test
    fun testMixingStandardAndCustomDurations() = runTest {
        val snackbarManager = SnackbarManager.getInstance()

        // When both duration and durationMillis are provided, durationMillis should take precedence
        snackbarManager.clearAll()
        SnapNotify.show("Test", SnackbarDuration.Long, 1500) // Custom should override standard
        assertEquals(1500L, snackbarManager.messages.value?.effectiveDuration?.getMilliseconds())

        // When only standard duration is provided
        snackbarManager.clearAll()
        SnapNotify.show("Test", SnackbarDuration.Indefinite)
        assertTrue(snackbarManager.messages.value?.effectiveDuration?.isIndefinite() == true)
        assertEquals(
            Long.MAX_VALUE,
            snackbarManager.messages.value?.effectiveDuration?.getMilliseconds()
        )
    }

    @Test
    fun testSnackbarDurationWrapperDataClassProperties() {
        // Test data class properties
        val custom1 = SnackbarDurationWrapper.Custom(5000)
        val custom2 = SnackbarDurationWrapper.Custom(5000)
        val custom3 = SnackbarDurationWrapper.Custom(3000)

        assertEquals(custom1, custom2)
        assertNotEquals(custom1, custom3)
        assertEquals(custom1.hashCode(), custom2.hashCode())
        assertNotEquals(custom1.hashCode(), custom3.hashCode())

        val standard1 = SnackbarDurationWrapper.Standard(SnackbarDuration.Short)
        val standard2 = SnackbarDurationWrapper.Standard(SnackbarDuration.Short)
        val standard3 = SnackbarDurationWrapper.Standard(SnackbarDuration.Long)

        assertEquals(standard1, standard2)
        assertNotEquals(standard1, standard3)
        assertEquals(standard1.hashCode(), standard2.hashCode())
        assertNotEquals(standard1.hashCode(), standard3.hashCode())

        // Test toString() methods work (data class auto-generated)
        assertTrue(custom1.toString().contains("5000"))
        assertTrue(standard1.toString().contains("Short"))
    }

    @Test
    fun testLargeCustomDurations() {
        // Test very large durations
        val largeButNotMax = Long.MAX_VALUE - 1
        val largeDuration = SnackbarDurationWrapper.fromMillis(largeButNotMax)
        assertEquals(largeButNotMax, largeDuration.getMilliseconds())
        assertFalse(largeDuration.isIndefinite()) // Should not be indefinite unless exactly MAX_VALUE

        val maxDuration = SnackbarDurationWrapper.fromMillis(Long.MAX_VALUE)
        assertTrue(maxDuration.isIndefinite())
    }
}