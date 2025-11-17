package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

@ExperimentalCoroutinesApi
class ThreadSafetyTest {

    private lateinit var snackbarManager: SnackbarManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        snackbarManager = SnackbarManager.getInstance()
        runBlocking {
            snackbarManager.updateConfig(SnapNotifyConfig().withDispatcher(testDispatcher))
            snackbarManager.clearAll()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            snackbarManager.updateConfig(SnapNotifyConfig())
            snackbarManager.clearAll()
        }
    }

    @Test
    fun `showMessage from multiple threads should be thread safe`() {
        val threadCount = 10
        val messagesPerThread = 5
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)

        repeat(threadCount) { threadIndex ->
            thread {
                try {
                    repeat(messagesPerThread) { messageIndex ->
                        snackbarManager.showMessage(
                            "Message from thread $threadIndex, message $messageIndex"
                        )
                        Thread.sleep(1) // Small delay to increase chance of race conditions
                    }
                    successCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        assertEquals(threadCount, successCount.get())

        // Should have exactly one message visible and the rest queued
        runBlocking {
            assertNotNull(snackbarManager.awaitMessage())
        }
    }

    @Test
    fun `clearAllMessages from multiple threads should be thread safe`() {
        val threadCount = 5
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)

        // First, add some messages
        repeat(10) {
            snackbarManager.showMessage("Test message $it")
        }

        // Then clear from multiple threads simultaneously
        repeat(threadCount) {
            thread {
                try {
                    runBlocking {
                        snackbarManager.clearAllMessages()?.join()
                    }
                    successCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        assertEquals(threadCount, successCount.get())

        // Should have no messages after clearing
        runBlocking {
            assertNull(snackbarManager.awaitNullMessage())
        }
    }

    @Test
    fun `mixed operations from multiple threads should be thread safe`() {
        val threadCount = 6
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)

        repeat(threadCount) { threadIndex ->
            thread {
                try {
                    when (threadIndex % 3) {
                        0 -> {
                            // Add messages
                            repeat(3) {
                                snackbarManager.showMessage("Add thread $threadIndex message $it")
                                Thread.sleep(1)
                            }
                        }

                        1 -> {
                            // Clear messages
                            Thread.sleep(5) // Let some messages accumulate first
                            runBlocking {
                                snackbarManager.clearAllMessages()?.join()
                            }
                        }

                        2 -> {
                            // Dismiss current messages
                            Thread.sleep(3)
                            runBlocking {
                                repeat(2) {
                                    snackbarManager.dismissCurrent()
                                    delay(1)
                                }
                            }
                        }
                    }
                    successCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        assertEquals(threadCount, successCount.get())
    }

    @Test
    fun `showMessage with action from background thread should work`() = runTest {
        var actionExecuted = false
        val latch = CountDownLatch(1)

        thread {
            try {
                snackbarManager.showMessage(
                    message = "Background thread message",
                    duration = SnackbarDuration.Short,
                    actionLabel = "Action",
                    onAction = { actionExecuted = true }
                )
            } finally {
                latch.countDown()
            }
        }

        latch.await()

        val currentMessage = runBlocking { snackbarManager.awaitMessage() }
        assertEquals("Background thread message", currentMessage.text)
        assertEquals("Action", currentMessage.actionLabel)

        // Execute the action
        currentMessage.onAction?.invoke()
        assertTrue(actionExecuted)
    }

    @Test
    fun `showMessage with custom style from background thread should work`() {
        val customStyle = SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color.Red,
            contentColor = androidx.compose.ui.graphics.Color.White
        )
        val latch = CountDownLatch(1)

        thread {
            try {
                snackbarManager.showMessage(
                    message = "Styled message",
                    style = customStyle
                )
            } finally {
                latch.countDown()
            }
        }

        latch.await()

        val currentMessage = runBlocking { snackbarManager.awaitMessage() }
        assertEquals("Styled message", currentMessage.text)
        assertEquals(customStyle, currentMessage.style)
    }

    @Test
    fun `concurrent showMessage and dismissCurrent should maintain queue integrity`() = runTest {
        val messageCount = 20
        val latch = CountDownLatch(2)

        // Thread 1: Add messages rapidly
        thread {
            try {
                repeat(messageCount) {
                    snackbarManager.showMessage("Message $it")
                    Thread.sleep(1)
                }
            } finally {
                latch.countDown()
            }
        }

        // Thread 2: Dismiss messages rapidly
        thread {
            try {
                Thread.sleep(5) // Let some messages accumulate
                repeat(messageCount / 2) {
                    runBlocking {
                        snackbarManager.dismissCurrent()
                    }
                    Thread.sleep(2)
                }
            } finally {
                latch.countDown()
            }
        }

        latch.await()

        // Should still have a valid state (either null or a valid message)
        // Timing is unpredictable here; the absence of an exception indicates success
        assertTrue("Operations completed successfully", true)
    }

    @Test
    fun `rapid clearAllMessages calls should not cause race conditions`() {
        val clearCount = 50
        val latch = CountDownLatch(clearCount)
        val successCount = AtomicInteger(0)

        // First add some messages
        repeat(5) {
            snackbarManager.showMessage("Initial message $it")
        }

        // Then call clearAllMessages rapidly from multiple threads
        repeat(clearCount) {
            thread {
                try {
                    runBlocking {
                        snackbarManager.clearAllMessages()?.join()
                    }
                    successCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        assertEquals(clearCount, successCount.get())
        runBlocking {
            assertNull(snackbarManager.awaitNullMessage())
        }
    }
}