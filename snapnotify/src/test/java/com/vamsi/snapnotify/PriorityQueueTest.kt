package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PriorityQueueTest {

    private lateinit var snackbarManager: SnackbarManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() = runBlocking {
        snackbarManager = SnackbarManager.getInstance()
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                deduplicationStrategy = DeduplicationStrategy.None
            ).withDispatcher(testDispatcher)
        )
        snackbarManager.clearAll()
    }

    @After
    fun tearDown() = runBlocking {
        snackbarManager.updateConfig(SnapNotifyConfig())
        snackbarManager.clearAll()
    }

    @Test
    fun `higher priority messages are dequeued before lower priority messages`() = runTest {
        // Active message
        snackbarManager.show("Active", priority = SnackbarPriority.Normal)
        assertEquals("Active", snackbarManager.awaitMessage().text)

        // Queue messages in non-priority order
        snackbarManager.show("Low Message", priority = SnackbarPriority.Low)
        snackbarManager.show("High Message", priority = SnackbarPriority.High)
        snackbarManager.show("Normal Message", priority = SnackbarPriority.Normal)

        // Dismiss active
        snackbarManager.dismissCurrent()
        assertEquals("High Message", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("Normal Message", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("Low Message", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `messages with same priority maintain FIFO ordering`() = runTest {
        snackbarManager.show("Active")

        snackbarManager.show("High 1", priority = SnackbarPriority.High)
        snackbarManager.show("High 2", priority = SnackbarPriority.High)
        snackbarManager.show("High 3", priority = SnackbarPriority.High)

        snackbarManager.dismissCurrent()
        assertEquals("High 1", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("High 2", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("High 3", snackbarManager.awaitMessage().text)
    }

    @Test
    fun `urgent message preempts currently active lower priority message`() = runTest {
        // Show normal active message
        snackbarManager.show("Normal Active", priority = SnackbarPriority.Normal)
        assertEquals("Normal Active", snackbarManager.awaitMessage().text)

        // Queue a regular high message
        snackbarManager.show("Queued High", priority = SnackbarPriority.High)

        // Fire urgent message
        snackbarManager.show("Urgent Alert", priority = SnackbarPriority.Urgent)

        // Urgent alert should immediately preempt active message
        val current = snackbarManager.awaitMessage()
        assertEquals("Urgent Alert", current.text)
        assertEquals(SnackbarPriority.Urgent, current.priority)

        // Dismiss urgent alert -> next should be Queued High
        snackbarManager.dismissCurrent()
        assertEquals("Queued High", snackbarManager.awaitMessage().text)

        // Dismiss Queued High -> the preempted Normal Active should have been re-queued
        snackbarManager.dismissCurrent()
        assertEquals("Normal Active", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `urgent does not preempt an already urgent message`() = runTest {
        snackbarManager.show("Urgent 1", priority = SnackbarPriority.Urgent)
        assertEquals("Urgent 1", snackbarManager.awaitMessage().text)

        snackbarManager.show("Urgent 2", priority = SnackbarPriority.Urgent)

        // Urgent 1 remains active
        assertEquals("Urgent 1", snackbarManager.awaitMessage().text)

        // Dismiss Urgent 1 -> Urgent 2 appears next
        snackbarManager.dismissCurrent()
        assertEquals("Urgent 2", snackbarManager.awaitMessage().text)
    }

    @Test
    fun `queue overflow evicts lowest priority oldest message first`() = runTest {
        val dropped = mutableListOf<String>()
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                maxQueueSize = 2,
                deduplicationStrategy = DeduplicationStrategy.None,
                onMessageDropped = { msg: String -> dropped.add(msg) }
            )
        )

        snackbarManager.show("Active")

        // Queue: [High 1, Low 1] (size = 2)
        snackbarManager.show("High 1", priority = SnackbarPriority.High)
        snackbarManager.show("Low 1", priority = SnackbarPriority.Low)

        // Enqueue High 2 -> Should evict Low 1 instead of High 1
        snackbarManager.show("High 2", priority = SnackbarPriority.High)
        assertEquals(listOf("Low 1"), dropped)

        // Queue should now contain High 1, High 2
        snackbarManager.dismissCurrent()
        assertEquals("High 1", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("High 2", snackbarManager.awaitMessage().text)
    }

    @Test
    fun `SnapNotify showUrgent helper works correctly`() = runTest {
        SnapNotify.show("Initial message")
        assertEquals("Initial message", snackbarManager.awaitMessage().text)

        SnapNotify.showUrgent("Urgent helper message")
        val active = snackbarManager.awaitMessage()
        assertEquals("Urgent helper message", active.text)
        assertEquals(SnackbarPriority.Urgent, active.priority)
    }

    @Test
    fun `urgent preemption drops low active message instead of high queued message when queue is full`() = runTest {
        val dropped = mutableListOf<String>()
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                maxQueueSize = 2,
                deduplicationStrategy = DeduplicationStrategy.None,
                onMessageDropped = { msg: String -> dropped.add(msg) }
            )
        )

        // Active message is Low
        snackbarManager.show("Active Low", priority = SnackbarPriority.Low)
        assertEquals("Active Low", snackbarManager.awaitMessage().text)

        // Queue is filled with High priority messages
        snackbarManager.show("Queued High 1", priority = SnackbarPriority.High)
        snackbarManager.show("Queued High 2", priority = SnackbarPriority.High)

        // Urgent arrives: Active Low should be dropped to preserve Queued High 1 & 2
        snackbarManager.show("Urgent Alert", priority = SnackbarPriority.Urgent)

        assertEquals(listOf("Active Low"), dropped)
        assertEquals("Urgent Alert", snackbarManager.awaitMessage().text)

        // Queued High messages remain intact
        snackbarManager.dismissCurrent()
        assertEquals("Queued High 1", snackbarManager.awaitMessage().text)

        snackbarManager.dismissCurrent()
        assertEquals("Queued High 2", snackbarManager.awaitMessage().text)
    }
}
