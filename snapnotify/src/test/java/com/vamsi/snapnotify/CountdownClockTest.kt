package com.vamsi.snapnotify

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountdownClockTest {

    @Test
    fun `countdown reaches zero after its duration`() = runTest {
        var progress = 1f

        runCountdown(durationMillis = 1_000L, isPaused = { false }, onProgress = { progress = it })

        assertEquals(0f, progress)
        assertEquals(1_000L, testScheduler.currentTime)
    }

    @Test
    fun `paused countdown does not finish while time passes`() = runTest {
        var paused = true
        var progress = 1f
        val job = launch {
            runCountdown(durationMillis = 1_000L, isPaused = { paused }, onProgress = { progress = it })
        }

        advanceTimeBy(1_000L)
        assertFalse(job.isCompleted)
        assertEquals(1f, progress)

        paused = false
        advanceTimeBy(1_000L)
        job.join()

        assertTrue(job.isCompleted)
        assertEquals(0f, progress)
    }
}
