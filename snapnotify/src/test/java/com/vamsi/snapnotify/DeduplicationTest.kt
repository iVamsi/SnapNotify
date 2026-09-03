package com.vamsi.snapnotify

import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeduplicationTest {

    private lateinit var snackbarManager: SnackbarManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() = runBlocking {
        snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()
    }

    @After
    fun tearDown() = runBlocking {
        snackbarManager.updateConfig(SnapNotifyConfig())
        snackbarManager.clearAll()
    }

    @Test
    fun `strategy None allows duplicate messages`() = runTest {
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                deduplicationStrategy = DeduplicationStrategy.None
            ).withDispatcher(testDispatcher)
        )

        snackbarManager.show("Same message")
        snackbarManager.show("Same message")

        assertEquals("Same message", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("Same message", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `strategy DropDuplicate drops duplicate of active message`() = runTest {
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                deduplicationStrategy = DeduplicationStrategy.DropDuplicate
            ).withDispatcher(testDispatcher)
        )

        snackbarManager.show("Active message")
        assertEquals("Active message", snackbarManager.awaitMessage().text)

        // Attempt duplicate
        snackbarManager.show("Active message")

        // Dismiss active -> queue should be empty because duplicate was dropped
        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `strategy DropDuplicate drops duplicate of queued message`() = runTest {
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                deduplicationStrategy = DeduplicationStrategy.DropDuplicate
            ).withDispatcher(testDispatcher)
        )

        snackbarManager.show("Active message")
        snackbarManager.show("Queued message")
        // Attempt duplicate of queued
        snackbarManager.show("Queued message")

        snackbarManager.dismissCurrent()
        assertEquals("Queued message", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `strategy ReplaceExisting refreshes currently active message`() = runTest {
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                deduplicationStrategy = DeduplicationStrategy.ReplaceExisting
            ).withDispatcher(testDispatcher)
        )

        snackbarManager.show("Repeating notification")
        val firstMessage = snackbarManager.awaitMessage()
        assertEquals("Repeating notification", firstMessage.text)

        // Sending same message with ReplaceExisting should refresh active with new ID
        snackbarManager.show("Repeating notification")
        val refreshedMessage = snackbarManager.awaitMessage()
        assertEquals("Repeating notification", refreshedMessage.text)
        assertNotEquals(firstMessage.id, refreshedMessage.id)

        // Queue was not polluted with a second message
        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `strategy ReplaceExisting replaces queued message with new instance`() = runTest {
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                deduplicationStrategy = DeduplicationStrategy.ReplaceExisting
            ).withDispatcher(testDispatcher)
        )

        snackbarManager.show("Active")

        // Enqueue normal priority
        snackbarManager.show("Task in progress", priority = SnackbarPriority.Low)

        // Replace with high priority
        snackbarManager.show("Task in progress", priority = SnackbarPriority.High)

        snackbarManager.dismissCurrent()
        val next = snackbarManager.awaitMessage()
        assertEquals("Task in progress", next.text)
        assertEquals(SnackbarPriority.High, next.priority)

        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `strategy ReplaceExisting bumps equal-priority duplicates to the top of the queue`() = runTest {
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                deduplicationStrategy = DeduplicationStrategy.ReplaceExisting
            ).withDispatcher(testDispatcher)
        )

        snackbarManager.show("Active")

        // Enqueue three messages with the same priority
        snackbarManager.show("Task 1", priority = SnackbarPriority.High)
        snackbarManager.show("Task 2", priority = SnackbarPriority.High)
        snackbarManager.show("Task 3", priority = SnackbarPriority.High)

        // Replace Task 2 with same priority -> should be bumped to the top of High priority messages
        snackbarManager.show("Task 2", priority = SnackbarPriority.High)

        snackbarManager.dismissCurrent()
        // Task 2 must be dequeued first
        assertEquals("Task 2", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("Task 1", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("Task 3", snackbarManager.awaitMessage().text)
    }
}
