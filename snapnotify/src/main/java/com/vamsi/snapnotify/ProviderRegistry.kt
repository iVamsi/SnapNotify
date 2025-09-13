package com.vamsi.snapnotify

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Registry to track active SnapNotifyProviders and prevent duplicates.
 * This ensures that only one provider in the composition hierarchy handles messages.
 */
internal object ProviderRegistry {
    private var activeProviderCount = 0
    
    /**
     * Registers a new provider. Returns true if this should be the active provider.
     */
    fun registerProvider(): Boolean {
        activeProviderCount++
        return activeProviderCount == 1
    }
    
    /**
     * Unregisters a provider.
     */
    fun unregisterProvider() {
        if (activeProviderCount > 0) {
            activeProviderCount--
        }
    }
    
    /**
     * Resets the registry (useful for testing).
     */
    internal fun reset() {
        activeProviderCount = 0
    }
}

/**
 * CompositionLocal to track if we're already inside a SnapNotifyProvider.
 */
internal val LocalSnapNotifyProvider = staticCompositionLocalOf { false }
