package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class ErrorHandlingTest {

    @Test
    fun testInvalidDurationHandling() {
        // Test that invalid durations throw proper exceptions
        assertThrows(IllegalArgumentException::class.java) {
            SnackbarDurationWrapper.fromMillis(0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            SnackbarDurationWrapper.fromMillis(-1)
        }

        assertThrows(IllegalArgumentException::class.java) {
            SnackbarDurationWrapper.fromSeconds(0.0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            SnackbarDurationWrapper.fromSeconds(-0.1)
        }
    }

    @Test
    fun testNullHandlingSafety() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Test that null action handlers don't crash - this should be handled gracefully
        var nullAction: (() -> Unit)? = null
        SnapNotify.show("Test", "Action", { nullAction?.invoke() })

        val message = snackbarManager.messages.value
        assertNotNull(message)
        assertEquals("Test", message?.text)
        assertEquals("Action", message?.actionLabel)
        assertNotNull(message?.onAction)
    }

    @Test
    fun testEmptyStrings() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Test empty message
        SnapNotify.show("")
        val emptyMessage = snackbarManager.messages.value
        assertNotNull(emptyMessage)
        assertEquals("", emptyMessage?.text)

        snackbarManager.clearAll()

        // Test empty action label
        SnapNotify.show("Message", "", {})
        val emptyActionMessage = snackbarManager.messages.value
        assertNotNull(emptyActionMessage)
        assertEquals("Message", emptyActionMessage?.text)
        assertEquals("", emptyActionMessage?.actionLabel)
    }

    @Test
    fun testManagerSingletonBehavior() {
        val manager1 = SnackbarManager.getInstance()
        val manager2 = SnackbarManager.getInstance()

        assertSame("SnackbarManager should be a singleton", manager1, manager2)
    }

    @Test
    fun testCustomDurationWrapperSealedClassExhaustiveness() {
        val standardWrapper = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Short)
        val customWrapper = SnackbarDurationWrapper.fromMillis(1000)

        // Test that all sealed class branches are handled
        val standardResult = when (standardWrapper) {
            is SnackbarDurationWrapper.Standard -> "standard"
            is SnackbarDurationWrapper.Custom -> "custom"
        }
        assertEquals("standard", standardResult)

        val customResult = when (customWrapper) {
            is SnackbarDurationWrapper.Standard -> "standard"
            is SnackbarDurationWrapper.Custom -> "custom"
        }
        assertEquals("custom", customResult)
    }

    @Test
    fun testVeryLongMessages() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        val longMessage = "A".repeat(1000) // Very long message
        SnapNotify.show(longMessage, durationMillis = 1000)

        val message = snackbarManager.messages.value
        assertNotNull(message)
        assertEquals(longMessage, message?.text)
        assertEquals(1000L, message?.effectiveDuration?.getMilliseconds())
    }

    @Test
    fun testRapidFireMessages() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Send many messages quickly
        repeat(10) { i ->
            SnapNotify.show("Message $i", durationMillis = 100L + i * 100L)
        }

        // Should have first message active and others queued
        val currentMessage = snackbarManager.messages.value
        assertNotNull(currentMessage)
        assertEquals("Message 0", currentMessage?.text)
        assertEquals(100L, currentMessage?.effectiveDuration?.getMilliseconds())
    }

    @Test
    fun testAllThemedMethodsWithCustomDuration() = runTest {
        val snackbarManager = SnackbarManager.getInstance()

        // Test all themed methods have custom duration support
        val testCases = listOf(
            { SnapNotify.showSuccess("Success", durationMillis = 1000) },
            { SnapNotify.showError("Error", durationMillis = 2000) },
            { SnapNotify.showWarning("Warning", durationMillis = 3000) },
            { SnapNotify.showInfo("Info", durationMillis = 4000) }
        )

        val expectedDurations = listOf(1000L, 2000L, 3000L, 4000L)

        testCases.forEachIndexed { index, testCase ->
            snackbarManager.clearAll()
            testCase()

            val message = snackbarManager.messages.value
            assertNotNull("Message should not be null for test case $index", message)
            assertEquals(expectedDurations[index], message?.effectiveDuration?.getMilliseconds())
            assertNotNull("Style should not be null for themed message", message?.style)
        }
    }

    @Test
    fun testAllThemedMethodsWithActions() = runTest {
        val snackbarManager = SnackbarManager.getInstance()

        var actionCalled = false
        val action = { actionCalled = true }

        // Test all themed methods with actions
        val testCases = listOf(
            { SnapNotify.showSuccess("Success", "OK", action, durationMillis = 1000) },
            { SnapNotify.showError("Error", "Retry", action, durationMillis = 2000) },
            { SnapNotify.showWarning("Warning", "Dismiss", action, durationMillis = 3000) },
            { SnapNotify.showInfo("Info", "Got it", action, durationMillis = 4000) }
        )

        val expectedLabels = listOf("OK", "Retry", "Dismiss", "Got it")
        val expectedDurations = listOf(1000L, 2000L, 3000L, 4000L)

        testCases.forEachIndexed { index, testCase ->
            snackbarManager.clearAll()
            actionCalled = false
            testCase()

            val message = snackbarManager.messages.value
            assertNotNull("Message should not be null for test case $index", message)
            assertEquals(expectedLabels[index], message?.actionLabel)
            assertEquals(expectedDurations[index], message?.effectiveDuration?.getMilliseconds())
            assertNotNull("Style should not be null for themed message", message?.style)
            assertNotNull("Action should not be null", message?.onAction)

            // Test action execution
            message?.onAction?.invoke()
            assertTrue("Action should have been called for test case $index", actionCalled)
        }
    }
}