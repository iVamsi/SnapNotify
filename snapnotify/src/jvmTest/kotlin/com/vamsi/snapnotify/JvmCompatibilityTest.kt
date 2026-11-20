package com.vamsi.snapnotify

import org.junit.Assert.assertSame
import org.junit.Test
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class JvmCompatibilityTest {

    @Test
    fun `factory caches the same channel under thread-pool contention`() {
        val id = UUID.randomUUID().toString()
        val pool = Executors.newFixedThreadPool(8)
        try {
            val channels = pool.invokeAll(List(100) { Callable { SnapNotify.channel(id) } })
            val first = channels.first().get()
            channels.forEach { assertSame(first, it.get()) }
        } finally {
            pool.shutdownNow()
        }
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
        facade.getDeclaredMethod(
            "SnapNotifyProvider",
            *(legacyTypes + NotificationPlacement::class.java + tail).toTypedArray()
        )
    }

    @Test
    fun `SnapNotifyConfig retains 1_0 copy ABI bridges`() {
        val configClass = SnapNotifyConfig::class.java
        val functionClass = Class.forName("kotlin.jvm.functions.Function1")

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
