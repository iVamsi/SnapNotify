package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration

/**
 * Wrapper class that allows both standard SnackbarDuration and custom millisecond durations.
 * 
 * This class provides backward compatibility while enabling custom duration support.
 * 
 * Usage examples:
 * ```
 * // Standard durations
 * val shortDuration = SnackbarDurationWrapper.fromStandard(SnackbarDuration.Short)
 * 
 * // Custom durations
 * val customDuration = SnackbarDurationWrapper.fromMillis(5000) // 5 seconds
 * ```
 */
sealed class SnackbarDurationWrapper {
    
    /**
     * Represents a standard Material Design duration.
     */
    data class Standard(val duration: SnackbarDuration) : SnackbarDurationWrapper()
    
    /**
     * Represents a custom duration in milliseconds.
     */
    data class Custom(val millis: Long) : SnackbarDurationWrapper()
    
    companion object {
        /**
         * Creates a wrapper for a standard SnackbarDuration.
         */
        fun fromStandard(duration: SnackbarDuration): SnackbarDurationWrapper {
            return Standard(duration)
        }
        
        /**
         * Creates a wrapper for a custom duration in milliseconds.
         * 
         * @param milliseconds Duration in milliseconds. Must be positive.
         * @throws IllegalArgumentException if milliseconds is not positive.
         */
        fun fromMillis(milliseconds: Long): SnackbarDurationWrapper {
            require(milliseconds > 0) { "Duration must be positive, got: $milliseconds" }
            return Custom(milliseconds)
        }
        
        /**
         * Creates a wrapper for a custom duration in seconds.
         * 
         * @param seconds Duration in seconds. Must be positive.
         * @throws IllegalArgumentException if seconds is not positive.
         */
        fun fromSeconds(seconds: Double): SnackbarDurationWrapper {
            require(seconds > 0) { "Duration must be positive, got: $seconds" }
            return Custom((seconds * 1000).toLong())
        }
    }
    
    /**
     * Returns the standard SnackbarDuration if this is a Standard wrapper,
     * or null if this is a Custom wrapper.
     */
    fun getStandardDuration(): SnackbarDuration? {
        return when (this) {
            is Standard -> duration
            is Custom -> null
        }
    }
    
    /**
     * Returns the duration in milliseconds.
     * For standard durations, returns approximate values based on Material Design specs.
     */
    fun getMilliseconds(): Long {
        return when (this) {
            is Standard -> when (duration) {
                SnackbarDuration.Short -> 4000L
                SnackbarDuration.Long -> 10000L
                SnackbarDuration.Indefinite -> Long.MAX_VALUE
            }
            is Custom -> millis
        }
    }
    
    /**
     * Returns true if this represents an indefinite duration.
     */
    fun isIndefinite(): Boolean {
        return when (this) {
            is Standard -> duration == SnackbarDuration.Indefinite
            is Custom -> millis == Long.MAX_VALUE
        }
    }
}
