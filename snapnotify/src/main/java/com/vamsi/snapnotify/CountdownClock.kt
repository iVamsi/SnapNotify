package com.vamsi.snapnotify

import kotlinx.coroutines.delay

/**
 * Counts [durationMillis] down to zero and reports the remaining fraction to [onProgress].
 * Time spent while [isPaused] returns true does not count.
 */
internal suspend fun runCountdown(
    durationMillis: Long,
    isPaused: () -> Boolean,
    onProgress: (Float) -> Unit,
) {
    var remaining = durationMillis
    while (remaining > 0) {
        val slice = minOf(COUNTDOWN_TICK_MILLIS, remaining)
        delay(slice)
        if (!isPaused()) {
            remaining -= slice
            onProgress(remaining.toFloat() / durationMillis)
        }
    }
}

internal const val COUNTDOWN_TICK_MILLIS = 16L
