package com.vamsi.snapnotify.di

import com.vamsi.snapnotify.core.SnackbarManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for accessing SnapNotify dependencies from static contexts.
 * 
 * This entry point allows the SnapNotify object to access the SnackbarManager
 * without requiring direct injection in non-Hilt managed classes.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SnapNotifyEntryPoint {
    fun snackbarManager(): SnackbarManager
}