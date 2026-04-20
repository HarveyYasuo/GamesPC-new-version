package com.harvey.gamespc.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun ProvideSafeFocusManager(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current
    CompositionLocalProvider(
        LocalFocusManager provides SafeFocusManager(focusManager)
    ) {
        content()
    }
}

private class SafeFocusManager(private val delegate: FocusManager) : FocusManager {
    override fun clearFocus(force: Boolean) {
        try {
            delegate.clearFocus(force)
        } catch (e: IllegalArgumentException) {
            // Ignorar la excepción específica para evitar el crash
            logHandledException(e)
        }
    }

    override fun moveFocus(focusDirection: FocusDirection): Boolean {
        return try {
            delegate.moveFocus(focusDirection)
        } catch (e: IllegalArgumentException) {
            // Ignorar la excepción específica para evitar el crash
            logHandledException(e)
            false
        }
    }

    private fun logHandledException(e: IllegalArgumentException) {
        // Opcional: Loggear la excepción si quieres monitorearla
        println("Handled IllegalArgumentException in SafeFocusManager: ${e.message}")
    }
}
