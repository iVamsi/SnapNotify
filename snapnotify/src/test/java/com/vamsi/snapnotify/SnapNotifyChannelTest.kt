package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
class SnapNotifyChannelTest {
    private fun channel() = SnapNotify.channel(UUID.randomUUID().toString()).also {
        it.configure(SnapNotifyConfig().withDispatcher(UnconfinedTestDispatcher()))
    }

    @Test
    fun `factory caches the same channel under concurrent access`() {
        val id = UUID.randomUUID().toString()
        val pool = Executors.newFixedThreadPool(8)
        try {
            val channels = pool.invokeAll(List(100) { java.util.concurrent.Callable { SnapNotify.channel(id) } })
            val first = channels.first().get()
            channels.forEach { assertSame(first, it.get()) }
        } finally {
            pool.shutdownNow()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank channel IDs are rejected`() {
        SnapNotify.channel("  ")
    }

    @Test
    fun `provider retains the 1_1 and 1_2 Compose JVM signatures`() {
        val legacyTypes = listOf(
            androidx.compose.ui.Modifier::class.java,
            SnackbarStyle::class.java,
            SnapNotifyConfig::class.java,
            androidx.compose.ui.Alignment::class.java,
            androidx.compose.foundation.layout.WindowInsets::class.java,
            Class.forName("kotlin.jvm.functions.Function5"),
        )
        val tail = listOf(
            Class.forName("kotlin.jvm.functions.Function2"),
            androidx.compose.runtime.Composer::class.java,
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
        )
        val facade = Class.forName("com.vamsi.snapnotify.SnapNotifyProviderKt")
        facade.getDeclaredMethod("SnapNotifyProvider", *(legacyTypes + tail).toTypedArray())
        facade.getDeclaredMethod("SnapNotifyProvider", *(legacyTypes + NotificationPlacement::class.java + tail).toTypedArray())
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
