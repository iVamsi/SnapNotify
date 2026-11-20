package com.vamsi.snapnotify

import com.vamsi.snapnotify.core.SnackbarManager
import com.vamsi.snapnotify.core.SnackbarMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ConcurrencyStressTest {

    private lateinit var snackbarManager: SnackbarManager

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
    fun `1000 coroutines concurrently inserting messages completes without deadlock or corruption`() = runBlocking {
        val totalMessages = 1000
        val maxQueue = 50
        val droppedCounter = AtomicInteger(0)

        snackbarManager.updateConfig(
            SnapNotifyConfig(
                maxQueueSize = maxQueue,
                deduplicationStrategy = DeduplicationStrategy.None,
                onMessageDropped = { droppedCounter.incrementAndGet() }
            )
        )

        // Launch 1,000 concurrent coroutines across background threads
        val jobs = (1..totalMessages).map { index ->
            async(Dispatchers.Default) {
                val priority = when (index % 4) {
                    0 -> SnackbarPriority.Low
                    1 -> SnackbarPriority.Normal
                    2 -> SnackbarPriority.High
                    else -> SnackbarPriority.Urgent
                }
                snackbarManager.show(
                    message = "Concurrent message #$index",
                    priority = priority
                )
            }
        }

        jobs.awaitAll()

        // Wait briefly for all emissions to settle
        delay(100)

        val active = snackbarManager.messages.value
        assertNotNull(active)

        // Drain the remaining queue
        var remainingCount = if (active != null) 1 else 0
        while (true) {
            snackbarManager.dismissCurrent()
            if (snackbarManager.messages.value != null) {
                remainingCount++
            } else {
                break
            }
        }

        // Total processed messages (dropped + remaining) must strictly equal 1,000
        val totalAccounted = droppedCounter.get() + remainingCount
        assertEquals(totalMessages, totalAccounted)
    }

    @Test
    fun `simulated unmount and cancellation cancels cleanly without leaking subsequent messages`() = runTest {
        snackbarManager.updateConfig(
            SnapNotifyConfig(deduplicationStrategy = DeduplicationStrategy.None)
        )

        // Show first message
        snackbarManager.show("Message 1")
        snackbarManager.show("Message 2")

        val message1 = snackbarManager.awaitMessage()
        assertEquals("Message 1", message1.text)

        // Simulate a LaunchedEffect job running for message 1
        var jobCompletedNormally = false
        val effectJob = launch {
            try {
                delay(10000) // simulating long duration
                jobCompletedNormally = true
            } finally {
                // If cancelled (unmount), dismissMessage is called
                snackbarManager.dismissMessage(message1)
            }
        }

        // Advance a bit, then unmount (cancel effectJob)
        delay(50)
        effectJob.cancelAndJoin()
        assertTrue(!jobCompletedNormally)

        // Queue must have smoothly transitioned to Message 2
        val message2 = snackbarManager.awaitMessage()
        assertEquals("Message 2", message2.text)

        // An outdated call to dismiss Message 1 should NOT dismiss Message 2!
        snackbarManager.dismissMessage(message1)
        assertEquals("Message 2", snackbarManager.messages.value?.text)

        // Only dismissing Message 2 should advance queue
        snackbarManager.dismissMessage(message2)
        assertEquals(null, snackbarManager.messages.value)
    }
}
