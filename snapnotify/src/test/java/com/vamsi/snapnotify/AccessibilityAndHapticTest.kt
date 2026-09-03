package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.vamsi.snapnotify.core.SnackbarMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityAndHapticTest {

    @Test
    fun `SnapNotifyVisuals holds all metadata correctly`() {
        val style = SnackbarStyle(containerColor = Color.Red)
        val visuals = SnapNotifyVisuals(
            message = "Test message",
            actionLabel = "Retry",
            withDismissAction = true,
            duration = SnackbarDuration.Long,
            style = style,
            isAssertive = true,
        )

        assertEquals("Test message", visuals.message)
        assertEquals("Retry", visuals.actionLabel)
        assertTrue(visuals.withDismissAction)
        assertEquals(SnackbarDuration.Long, visuals.duration)
        assertEquals(style, visuals.style)
        assertTrue(visuals.isAssertive)
    }

    @Test
    fun `isAssertiveAccessibility is decoupled from priority and true for Urgent or explicit isAssertive`() {
        val urgentMessage = SnackbarMessage(
            text = "Urgent",
            priority = SnackbarPriority.Urgent
        )
        assertTrue(urgentMessage.isAssertiveAccessibility)

        // High priority without isAssertive is polite, not assertive
        val highMessage = SnackbarMessage(
            text = "High Informational",
            priority = SnackbarPriority.High
        )
        assertFalse(highMessage.isAssertiveAccessibility)

        // Explicit isAssertive = true (e.g. from showError) is assertive
        val errorMessage = SnackbarMessage(
            text = "Error",
            isAssertive = true
        )
        assertTrue(errorMessage.isAssertiveAccessibility)

        val normalMessage = SnackbarMessage(
            text = "Normal",
            priority = SnackbarPriority.Normal
        )
        assertFalse(normalMessage.isAssertiveAccessibility)

        val lowMessage = SnackbarMessage(
            text = "Low",
            priority = SnackbarPriority.Low
        )
        assertFalse(lowMessage.isAssertiveAccessibility)
    }

    @Test
    fun `resolveHapticFeedback maps Auto correctly without color sniffing`() {
        // Explicit Error
        val errorMessage = SnackbarMessage(
            text = "Error",
            hapticFeedback = SnackbarHapticFeedback.Error
        )
        assertEquals(SnackbarHapticFeedback.Error, errorMessage.resolveHapticFeedback())

        // Explicit Success
        val successMessage = SnackbarMessage(
            text = "Success",
            hapticFeedback = SnackbarHapticFeedback.Success
        )
        assertEquals(SnackbarHapticFeedback.Success, successMessage.resolveHapticFeedback())

        // Explicit Warning
        val warningMessage = SnackbarMessage(
            text = "Warning",
            hapticFeedback = SnackbarHapticFeedback.Warning
        )
        assertEquals(SnackbarHapticFeedback.Warning, warningMessage.resolveHapticFeedback())

        // Urgent maps to Error vibration
        val urgentMessage = SnackbarMessage(
            text = "Urgent",
            priority = SnackbarPriority.Urgent,
            hapticFeedback = SnackbarHapticFeedback.Auto
        )
        assertEquals(SnackbarHapticFeedback.Error, urgentMessage.resolveHapticFeedback())

        // An assertive message maps to Error even at Normal priority
        val assertiveMessage = SnackbarMessage(
            text = "Assertive",
            isAssertive = true,
            hapticFeedback = SnackbarHapticFeedback.Auto
        )
        assertEquals(SnackbarHapticFeedback.Error, assertiveMessage.resolveHapticFeedback())

        // High maps to a warning buzz
        val highMessage = SnackbarMessage(
            text = "High",
            priority = SnackbarPriority.High,
            hapticFeedback = SnackbarHapticFeedback.Auto
        )
        assertEquals(SnackbarHapticFeedback.Warning, highMessage.resolveHapticFeedback())

        // Normal Auto maps to None
        val normalMessage = SnackbarMessage(
            text = "Normal",
            priority = SnackbarPriority.Normal,
            hapticFeedback = SnackbarHapticFeedback.Auto
        )
        assertEquals(SnackbarHapticFeedback.None, normalMessage.resolveHapticFeedback())

        // Low Auto maps to None
        val lowMessage = SnackbarMessage(
            text = "Low",
            priority = SnackbarPriority.Low,
            hapticFeedback = SnackbarHapticFeedback.Auto
        )
        assertEquals(SnackbarHapticFeedback.None, lowMessage.resolveHapticFeedback())

        // Explicit Gesture
        val gestureMessage = SnackbarMessage(
            text = "Custom",
            hapticFeedback = SnackbarHapticFeedback.Gesture
        )
        assertEquals(SnackbarHapticFeedback.Gesture, gestureMessage.resolveHapticFeedback())
    }

    @Test
    fun `SnapNotifyConfig defaults enable haptics and accessibility scaling`() {
        val config = SnapNotifyConfig()
        assertTrue(config.isHapticFeedbackEnabled)
        assertTrue(config.isAccessibilityScalingEnabled)
        assertEquals(DeduplicationStrategy.DropDuplicate, config.deduplicationStrategy)
        assertEquals(SnapNotifyConfig.DEFAULT_MAX_QUEUE_SIZE, config.maxQueueSize)
        assertEquals(null, config.onMessageDropped)
    }

    @Test
    fun `SnapNotifyConfig copy retains backward compatibility`() {
        val original = SnapNotifyConfig(maxQueueSize = 25)
        val copy2Arg = original.copy(maxQueueSize = 30, onMessageDropped = null)
        assertEquals(30, copy2Arg.maxQueueSize)
        assertEquals(DeduplicationStrategy.DropDuplicate, copy2Arg.deduplicationStrategy)
        assertTrue(copy2Arg.isHapticFeedbackEnabled)
        assertTrue(copy2Arg.isAccessibilityScalingEnabled)

        // Destructuring component1 and component2
        val (maxQueue, onDrop) = copy2Arg
        assertEquals(30, maxQueue)
        assertEquals(null, onDrop)
    }

    @Test
    fun `SnapNotifyConfig copy - given only queue fields changed - keeps the other options`() {
        val original = SnapNotifyConfig(
            deduplicationStrategy = DeduplicationStrategy.None,
            isHapticFeedbackEnabled = false,
            isAccessibilityScalingEnabled = false,
            maxQueueSize = 25,
            onMessageDropped = null
        )

        val copied = original.copy(maxQueueSize = 10, onMessageDropped = null)

        assertEquals(10, copied.maxQueueSize)
        assertEquals(DeduplicationStrategy.None, copied.deduplicationStrategy)
        assertFalse(copied.isHapticFeedbackEnabled)
        assertFalse(copied.isAccessibilityScalingEnabled)
    }

    @Test
    fun `computeAccessibleDuration scales by at least 2x when accessibility is enabled`() {
        val raw = 4000L

        // Both scaling and accessibility enabled -> at least 2x
        val scaledDefault = computeAccessibleDuration(
            rawDurationMillis = raw,
            isAccessibilityEnabled = true,
            isScalingConfigEnabled = true,
            recommendedTimeoutMillis = null
        )
        assertEquals(8000L, scaledDefault)

        // Platform recommended is larger -> uses recommended
        val scaledLarger = computeAccessibleDuration(
            rawDurationMillis = raw,
            isAccessibilityEnabled = true,
            isScalingConfigEnabled = true,
            recommendedTimeoutMillis = 15000L
        )
        assertEquals(15000L, scaledLarger)

        // Platform recommended is smaller than 2x -> roadmap minimum 2x is enforced
        val scaledSmaller = computeAccessibleDuration(
            rawDurationMillis = raw,
            isAccessibilityEnabled = true,
            isScalingConfigEnabled = true,
            recommendedTimeoutMillis = 5000L
        )
        assertEquals(8000L, scaledSmaller)

        // Accessibility disabled on system -> unchanged
        val disabledSystem = computeAccessibleDuration(
            rawDurationMillis = raw,
            isAccessibilityEnabled = false,
            isScalingConfigEnabled = true,
            recommendedTimeoutMillis = 15000L
        )
        assertEquals(4000L, disabledSystem)

        // Scaling disabled in config -> unchanged
        val disabledConfig = computeAccessibleDuration(
            rawDurationMillis = raw,
            isAccessibilityEnabled = true,
            isScalingConfigEnabled = false,
            recommendedTimeoutMillis = 15000L
        )
        assertEquals(4000L, disabledConfig)
    }

    @Test
    fun `computeAccessibleDuration - given a standard Short duration - stretches it for TalkBack`() {
        val short = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Short).getMilliseconds()

        val scaled = computeAccessibleDuration(
            rawDurationMillis = short,
            isAccessibilityEnabled = true,
            isScalingConfigEnabled = true,
            recommendedTimeoutMillis = null
        )

        assertEquals(short * 2, scaled)
        assertTrue(scaled > short)
    }

    @Test
    fun `SnapNotifyConfig retains 1_0 Kotlin ABI bridges`() {
        val configClass = SnapNotifyConfig::class.java
        val functionClass = Class.forName("kotlin.jvm.functions.Function1")
        val markerClass = Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")

        configClass.getDeclaredConstructor(
            Int::class.javaPrimitiveType,
            functionClass,
            Int::class.javaPrimitiveType,
            markerClass,
        )
        configClass.getDeclaredMethod(
            "copy",
            Int::class.javaPrimitiveType,
            functionClass,
        )
        configClass.getDeclaredMethod(
            "copy\$default",
            configClass,
            Int::class.javaPrimitiveType,
            functionClass,
            Int::class.javaPrimitiveType,
            Any::class.java,
        )
    }
}
