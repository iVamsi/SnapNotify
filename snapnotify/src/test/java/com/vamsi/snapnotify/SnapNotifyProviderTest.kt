package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SnapNotifyProviderTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() = runBlocking {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.updateConfig(SnapNotifyConfig().withDispatcher(testDispatcher))
        snackbarManager.clearAll()
    }

    @After
    fun tearDown() = runBlocking {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.updateConfig(SnapNotifyConfig())
        snackbarManager.clearAll()
    }

    @Test
    fun testSnackbarDurationLogicWithoutUI() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        // Test that duration logic works properly in manager
        SnapNotify.show("Standard message", SnackbarDuration.Short)

        val message = snackbarManager.awaitMessage()
        assertEquals("Standard message", message.text)
        assertEquals(SnackbarDuration.Short, message.effectiveDuration.getStandardDuration())
    }

    @Test
    fun testCustomDurationLogicWithoutUI() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        SnapNotify.show("Custom message", durationMillis = 5500)

        val message = snackbarManager.awaitMessage()
        assertEquals("Custom message", message.text)
        assertEquals(5500L, message.effectiveDuration.getMilliseconds())
        assertTrue(message.effectiveDuration is SnackbarDurationWrapper.Custom)
    }

    @Test
    fun testIndefiniteCustomDurationLogic() = runTest {
        val snackbarManager = SnackbarManager.getInstance()
        snackbarManager.clearAll()

        SnapNotify.show("Indefinite message", durationMillis = Long.MAX_VALUE)

        val message = snackbarManager.awaitMessage()
        assertEquals("Indefinite message", message.text)
        assertTrue(message.effectiveDuration.isIndefinite())
    }

    @Test
    fun testSnackbarStyleCreation() {
        // Test SnackbarStyle creation without UI dependencies
        val style = SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color.Blue,
            contentColor = androidx.compose.ui.graphics.Color.White
        )

        assertNotNull(style)
        assertEquals(androidx.compose.ui.graphics.Color.Blue, style.containerColor)
        assertEquals(androidx.compose.ui.graphics.Color.White, style.contentColor)
    }

}