package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive tests for the public SnapNotify API.
 */
@ExperimentalCoroutinesApi
class SnapNotifyTest {

    private lateinit var snackbarManager: SnackbarManager

    @Before
    fun setup() {
        // Initialize SnapNotify to ensure the singleton is created
        SnapNotify.initialize()
        snackbarManager = SnackbarManager.getInstance()
        // Clear any existing messages
        snackbarManager.clearAllMessages()
    }

    @After
    fun cleanup() {
        // Clear messages after each test
        snackbarManager.clearAllMessages()
    }

    // Basic show() methods
    @Test
    fun `show with message displays correctly`() = runTest {
        val message = "Test message"

        SnapNotify.show(message)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(SnackbarDuration.Short, currentMessage?.duration)
        assertNull(currentMessage?.actionLabel)
        assertNull(currentMessage?.onAction)
        assertNull(currentMessage?.style)
    }

    @Test
    fun `show with message and duration displays correctly`() = runTest {
        val message = "Long test message"
        val duration = SnackbarDuration.Long

        SnapNotify.show(message, duration)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(duration, currentMessage?.duration)
    }

    @Test
    fun `show with message and action displays correctly`() = runTest {
        val message = "Test message with action"
        val actionLabel = "Retry"
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.show(message, actionLabel, onAction)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(actionLabel, currentMessage?.actionLabel)
        assertEquals(SnackbarDuration.Short, currentMessage?.duration)

        // Test action execution
        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    @Test
    fun `show with message action and duration displays correctly`() = runTest {
        val message = "Test message"
        val actionLabel = "Action"
        val duration = SnackbarDuration.Long
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.show(message, actionLabel, onAction, duration)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(actionLabel, currentMessage?.actionLabel)
        assertEquals(duration, currentMessage?.duration)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    // showStyled() methods
    @Test
    fun `showStyled with custom style displays correctly`() = runTest {
        val message = "Styled message"
        val style = SnackbarStyle(
            containerColor = Color.Blue,
            contentColor = Color.White,
            actionColor = Color.Yellow
        )

        SnapNotify.showStyled(message, style)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(style, currentMessage?.style)
        assertEquals(SnackbarDuration.Short, currentMessage?.duration)
    }

    @Test
    fun `showStyled with style and action displays correctly`() = runTest {
        val message = "Styled message with action"
        val actionLabel = "OK"
        val style = SnackbarStyle(containerColor = Color.Green)
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.showStyled(message, style, actionLabel, onAction)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(actionLabel, currentMessage?.actionLabel)
        assertEquals(style, currentMessage?.style)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    @Test
    fun `showStyled with style action and duration displays correctly`() = runTest {
        val message = "Styled message"
        val actionLabel = "Action"
        val duration = SnackbarDuration.Indefinite
        val style = SnackbarStyle(containerColor = Color.Red)
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.showStyled(message, style, actionLabel, onAction, duration)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(actionLabel, currentMessage?.actionLabel)
        assertEquals(duration, currentMessage?.duration)
        assertEquals(style, currentMessage?.style)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    // showSuccess() methods
    @Test
    fun `showSuccess displays with success styling`() = runTest {
        val message = "Success message"

        SnapNotify.showSuccess(message)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(SnackbarDuration.Short, currentMessage?.duration)

        // Verify success colors
        val style = currentMessage?.style
        assertNotNull(style)
        assertEquals(Color(0xFF2E7D32), style?.containerColor) // Success green
        assertEquals(Color.White, style?.contentColor)
        assertEquals(Color(0xFF81C784), style?.actionColor) // Light green
    }

    @Test
    fun `showSuccess with action displays correctly`() = runTest {
        val message = "Success with action"
        val actionLabel = "View"
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.showSuccess(message, actionLabel, onAction)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(actionLabel, currentMessage?.actionLabel)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    @Test
    fun `showSuccess with duration displays correctly`() = runTest {
        val message = "Success message"
        val duration = SnackbarDuration.Long

        SnapNotify.showSuccess(message, duration)

        val currentMessage = snackbarManager.messages.first()
        assertEquals(duration, currentMessage?.duration)
    }

    // showError() methods  
    @Test
    fun `showError displays with error styling`() = runTest {
        val message = "Error message"

        SnapNotify.showError(message)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)

        // Verify error colors
        val style = currentMessage?.style
        assertNotNull(style)
        assertEquals(Color(0xFFD32F2F), style?.containerColor) // Error red
        assertEquals(Color.White, style?.contentColor)
        assertEquals(Color(0xFFEF5350), style?.actionColor) // Light red
    }

    @Test
    fun `showError with action displays correctly`() = runTest {
        val message = "Error with retry"
        val actionLabel = "Retry"
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.showError(message, actionLabel, onAction)

        val currentMessage = snackbarManager.messages.first()
        assertEquals(actionLabel, currentMessage?.actionLabel)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    // showWarning() methods
    @Test
    fun `showWarning displays with warning styling`() = runTest {
        val message = "Warning message"

        SnapNotify.showWarning(message)

        val currentMessage = snackbarManager.messages.first()
        assertEquals(message, currentMessage?.text)

        // Verify warning colors
        val style = currentMessage?.style
        assertNotNull(style)
        assertEquals(Color(0xFFE65100), style?.containerColor) // Warning orange
        assertEquals(Color.White, style?.contentColor)
        assertEquals(Color(0xFFFFB74D), style?.actionColor) // Light orange
    }

    @Test
    fun `showWarning with action displays correctly`() = runTest {
        val message = "Warning with action"
        val actionLabel = "Fix"
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.showWarning(message, actionLabel, onAction)

        val currentMessage = snackbarManager.messages.first()
        assertEquals(actionLabel, currentMessage?.actionLabel)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    // showInfo() methods
    @Test
    fun `showInfo displays with info styling`() = runTest {
        val message = "Info message"

        SnapNotify.showInfo(message)

        val currentMessage = snackbarManager.messages.first()
        assertEquals(message, currentMessage?.text)

        // Verify info colors
        val style = currentMessage?.style
        assertNotNull(style)
        assertEquals(Color(0xFF1976D2), style?.containerColor) // Info blue
        assertEquals(Color.White, style?.contentColor)
        assertEquals(Color(0xFF42A5F5), style?.actionColor) // Light blue
    }

    @Test
    fun `showInfo with action displays correctly`() = runTest {
        val message = "Info with action"
        val actionLabel = "Learn More"
        var actionCalled = false
        val onAction = { actionCalled = true }

        SnapNotify.showInfo(message, actionLabel, onAction)

        val currentMessage = snackbarManager.messages.first()
        assertEquals(actionLabel, currentMessage?.actionLabel)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    // clearAll() method
    @Test
    fun `clearAll removes all messages`() = runTest {
        // Add multiple messages
        SnapNotify.show("Message 1")
        SnapNotify.show("Message 2")
        SnapNotify.show("Message 3")

        // Verify messages exist
        assertNotNull(snackbarManager.messages.first())

        // Clear all
        SnapNotify.clearAll()

        // Verify all cleared
        assertNull(snackbarManager.messages.first())
    }

    // Integration tests
    @Test
    fun `multiple rapid calls queue messages correctly`() = runTest {
        SnapNotify.show("First")
        SnapNotify.showSuccess("Second")
        SnapNotify.showError("Third")

        // First message should be displayed
        assertEquals("First", snackbarManager.messages.first()?.text)

        // Dismiss and check next
        snackbarManager.dismissCurrent()
        assertEquals("Second", snackbarManager.messages.first()?.text)

        // Verify it has success styling
        val style = snackbarManager.messages.first()?.style
        assertEquals(Color(0xFF2E7D32), style?.containerColor)
    }

    @Test
    fun `mixed message types maintain correct styling`() = runTest {
        SnapNotify.showError("Error")
        snackbarManager.dismissCurrent()

        SnapNotify.showWarning("Warning")
        val currentMessage = snackbarManager.messages.first()

        // Verify warning styling is applied correctly
        val style = currentMessage?.style
        assertEquals(Color(0xFFE65100), style?.containerColor) // Warning orange
        assertEquals("Warning", currentMessage?.text)
    }
}