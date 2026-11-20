package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class SnapNotifyChannelTest {
    private fun channel() = SnapNotify.channel(Uuid.random().toString()).also {
        it.configure(SnapNotifyConfig().withDispatcher(UnconfinedTestDispatcher()))
    }

    @Test
    fun `factory caches the same channel under concurrent access`() = runTest {
        val id = Uuid.random().toString()
        val channels = List(100) {
            async(Dispatchers.Default) { SnapNotify.channel(id) }
        }.awaitAll()
        val first = channels.first()
        channels.forEach { assertSame(first, it) }
    }

    @Test
    fun `blank channel IDs are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            SnapNotify.channel("  ")
        }
    }

    @Test
    fun `deduplication priority overflow and clear are isolated`() = runTest {
        val first = channel()
        val second = channel()
        val dropped = mutableListOf<String>()
        first.configure(SnapNotifyConfig(
            maxQueueSize = 1, onMessageDropped = { dropped += it },
        ).withDispatcher(UnconfinedTestDispatcher()))
        first.show("Shared text")
        second.show("Shared text")
        first.show("Low", priority = SnackbarPriority.Low)
        first.show("High", priority = SnackbarPriority.High)
        assertEquals(listOf("Low"), dropped)
        first.showUrgent("Urgent")
        assertEquals("Urgent", first.manager.messages.value?.text)
        assertEquals("Shared text", second.manager.messages.value?.text)
        first.manager.dismissCurrent()
        assertEquals("High", first.manager.messages.value?.text)
        first.clear()
        assertNull(first.manager.messages.value)
        assertEquals("Shared text", second.manager.messages.value?.text)
        second.clear()
    }

    @Test
    fun `global operations do not affect a scoped queue`() = runTest {
        val global = SnackbarManager.getInstance()
        val oldConfig = global.getConfig()
        try {
            global.updateConfig(SnapNotifyConfig().withDispatcher(UnconfinedTestDispatcher()))
            global.clearAll()
            val scoped = channel()
            scoped.showError("Scoped error")
            SnapNotify.show("Global message")
            assertTrue(scoped.manager.messages.value!!.isAssertiveAccessibility)
            SnapNotify.clearAll()
            assertNull(global.messages.value)
            assertEquals("Scoped error", scoped.manager.messages.value?.text)
            scoped.clear()
        } finally {
            global.clearAll()
            global.updateConfig(oldConfig)
        }
    }

    @Test
    fun `scoped messages retain rich content and undo callbacks`() = runTest {
        val scoped = channel()
        var actionCalled = false
        var timeoutCalled = false
        scoped.show("Body", title = "Title", showCloseButton = true, durationMillis = 600)
        assertEquals("Title", scoped.manager.messages.value?.title)
        assertTrue(scoped.manager.messages.value!!.showCloseButton)
        assertEquals(600L, scoped.manager.messages.value!!.effectiveDuration.getMilliseconds())
        scoped.clear()
        scoped.showUndoable("Removed", onAction = { actionCalled = true }, onTimeout = { timeoutCalled = true })
        val message = scoped.manager.messages.value!!
        assertTrue(message.showProgressBar)
        assertEquals("Undo", message.actionLabel)
        message.onAction!!.invoke()
        message.onTimeout!!.invoke()
        assertTrue(actionCalled)
        assertTrue(timeoutCalled)
        scoped.clear()
    }

    @Test
    fun `custom duration normalizes the standard duration like the global API`() = runTest {
        val scoped = channel()
        scoped.show("Body", duration = SnackbarDuration.Indefinite, durationMillis = 600)
        assertEquals(SnackbarDuration.Short, scoped.manager.messages.value?.duration)
        scoped.clear()
    }

    @Test
    fun `global and channel helpers share status semantics`() = runTest {
        val global = SnackbarManager.getInstance()
        val oldConfig = global.getConfig()
        val scoped = channel()
        val calls = listOf<Pair<() -> Unit, () -> Unit>>(
            { SnapNotify.showSuccess("Message") } to { scoped.showSuccess("Message") },
            { SnapNotify.showError("Message") } to { scoped.showError("Message") },
            { SnapNotify.showWarning("Message") } to { scoped.showWarning("Message") },
            { SnapNotify.showInfo("Message") } to { scoped.showInfo("Message") },
            { SnapNotify.showUrgent("Message") } to { scoped.showUrgent("Message") },
            {
                SnapNotify.show("Message", priority = SnackbarPriority.Normal, hapticFeedback = SnackbarHapticFeedback.Error)
            } to {
                scoped.show("Message", hapticFeedback = SnackbarHapticFeedback.Error)
            },
        )
        try {
            global.updateConfig(SnapNotifyConfig().withDispatcher(UnconfinedTestDispatcher()))
            for ((globalShow, scopedShow) in calls) {
                global.clearAll()
                scoped.clear()
                globalShow()
                scopedShow()
                val expected = global.messages.value!!
                val actual = scoped.manager.messages.value!!
                assertEquals(expected.style, actual.style)
                assertEquals(expected.priority, actual.priority)
                assertEquals(expected.hapticFeedback, actual.hapticFeedback)
                assertEquals(expected.isAssertiveAccessibility, actual.isAssertiveAccessibility)
            }
            global.clearAll()
            SnapNotify.showError("Silent error", priority = SnackbarPriority.Normal, hapticFeedback = SnackbarHapticFeedback.None)
            assertEquals(SnackbarHapticFeedback.None, global.messages.value!!.hapticFeedback)
            assertTrue(global.messages.value!!.isAssertiveAccessibility)
        } finally {
            scoped.clear()
            global.clearAll()
            global.updateConfig(oldConfig)
        }
    }
}
