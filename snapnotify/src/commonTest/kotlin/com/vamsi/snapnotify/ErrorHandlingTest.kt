package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ErrorHandlingTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.updateConfig(SnapNotifyConfig().withDispatcher(testDispatcher))
        snackbarManager.clearAll()
    }

    @AfterTest
    fun tearDown() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.updateConfig(SnapNotifyConfig())
        snackbarManager.clearAll()
    }

    @Test
    fun testInvalidDurationHandling() {
        // Test that invalid durations throw proper exceptions
        assertFailsWith<IllegalArgumentException> {
            SnackbarDurationWrapper.fromMillis(0)
        }

        assertFailsWith<IllegalArgumentException> {
            SnackbarDurationWrapper.fromMillis(-1)
        }

        assertFailsWith<IllegalArgumentException> {
            SnackbarDurationWrapper.fromSeconds(0.0)
        }

        assertFailsWith<IllegalArgumentException> {
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

        val message = snackbarManager.awaitMessage()
        assertEquals("Test", message.text)
        assertEquals("Action", message.actionLabel)
        assertNotNull(message.onAction)
    }

    @Test
    fun testEmptyStrings() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Test empty message
        SnapNotify.show("")
        val emptyMessage = snackbarManager.awaitMessage()
        assertEquals("", emptyMessage.text)

        snackbarManager.clearAll()

        // Test empty action label
        SnapNotify.show("Message", "", {})
        val emptyActionMessage = snackbarManager.awaitMessage()
        assertEquals("Message", emptyActionMessage.text)
        assertEquals("", emptyActionMessage.actionLabel)
    }

    @Test
    fun testManagerSingletonBehavior() {
        val manager1 = SnackbarManager.getInstance()
        val manager2 = SnackbarManager.getInstance()

        assertSame(manager1, manager2, "SnackbarManager should be a singleton")
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

        val message = snackbarManager.awaitMessage()
        assertEquals(longMessage, message.text)
        assertEquals(1000L, message.effectiveDuration.getMilliseconds())
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
        val currentMessage = snackbarManager.awaitMessage()
        assertEquals("Message 0", currentMessage.text)
        assertEquals(100L, currentMessage.effectiveDuration.getMilliseconds())
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

            val message = snackbarManager.awaitMessage()
            assertEquals(
                expectedDurations[index],
                message.effectiveDuration.getMilliseconds(),
                "Unexpected duration for test case $index"
            )
            assertNotNull(message.style, "Style should not be null for themed message")
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

            val message = snackbarManager.awaitMessage()
            assertEquals(expectedLabels[index], message.actionLabel)
            assertEquals(expectedDurations[index], message.effectiveDuration.getMilliseconds())
            assertNotNull(message.style, "Style should not be null for themed message")
            assertNotNull(message.onAction, "Action should not be null")

            // Test action execution
            message.onAction.invoke()
            assertTrue(actionCalled, "Action should have been called for test case $index")
        }
    }
}