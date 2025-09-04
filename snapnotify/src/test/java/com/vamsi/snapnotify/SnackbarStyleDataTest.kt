package com.vamsi.snapnotify

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SnackbarStyle data class functionality (non-Compose tests).
 */
class SnackbarStyleDataTest {

    @Test
    fun `SnackbarStyle default constructor creates correct instance`() {
        val style = SnackbarStyle()
        
        assertEquals(Color.Unspecified, style.containerColor)
        assertEquals(Color.Unspecified, style.contentColor)
        assertEquals(Color.Unspecified, style.actionColor)
        assertNull(style.shape)
        assertNull(style.elevation)
        assertNull(style.messageTextStyle)
        assertNull(style.actionTextStyle)
    }

    @Test
    fun `SnackbarStyle custom constructor creates correct instance`() {
        val containerColor = Color.Blue
        val contentColor = Color.White
        val actionColor = Color.Yellow
        val shape = RoundedCornerShape(16.dp)
        val elevation = 8.dp
        val messageStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
        val actionStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
        
        val style = SnackbarStyle(
            containerColor = containerColor,
            contentColor = contentColor,
            actionColor = actionColor,
            shape = shape,
            elevation = elevation,
            messageTextStyle = messageStyle,
            actionTextStyle = actionStyle
        )
        
        assertEquals(containerColor, style.containerColor)
        assertEquals(contentColor, style.contentColor)
        assertEquals(actionColor, style.actionColor)
        assertEquals(shape, style.shape)
        assertEquals(elevation, style.elevation)
        assertEquals(messageStyle, style.messageTextStyle)
        assertEquals(actionStyle, style.actionTextStyle)
    }

    @Test
    fun `SnackbarStyle equality works correctly`() {
        val style1 = SnackbarStyle(
            containerColor = Color.Red,
            contentColor = Color.White,
            actionColor = Color.Blue
        )
        
        val style2 = SnackbarStyle(
            containerColor = Color.Red,
            contentColor = Color.White,
            actionColor = Color.Blue
        )
        
        val style3 = SnackbarStyle(
            containerColor = Color.Green,
            contentColor = Color.White,
            actionColor = Color.Blue
        )
        
        assertEquals(style1, style2)
        assertNotEquals(style1, style3)
    }

    @Test
    fun `SnackbarStyle copy works correctly`() {
        val originalStyle = SnackbarStyle(
            containerColor = Color.Red,
            contentColor = Color.White,
            actionColor = Color.Blue,
            elevation = 4.dp
        )
        
        val copiedStyle = originalStyle.copy(
            containerColor = Color.Green
        )
        
        assertEquals(Color.Green, copiedStyle.containerColor)
        assertEquals(Color.White, copiedStyle.contentColor) // Should remain the same
        assertEquals(Color.Blue, copiedStyle.actionColor) // Should remain the same
        assertEquals(4.dp, copiedStyle.elevation) // Should remain the same
    }

    @Test
    fun `SnackbarStyle toString provides useful information`() {
        val style = SnackbarStyle(
            containerColor = Color.Red,
            contentColor = Color.White
        )
        
        val stringRepresentation = style.toString()
        
        assertTrue(stringRepresentation.contains("SnackbarStyle"))
        assertTrue(stringRepresentation.contains("containerColor"))
        assertTrue(stringRepresentation.contains("contentColor"))
    }

    @Test
    fun `SnackbarStyle hashCode consistency`() {
        val style1 = SnackbarStyle(
            containerColor = Color.Red,
            contentColor = Color.White
        )
        
        val style2 = SnackbarStyle(
            containerColor = Color.Red,
            contentColor = Color.White
        )
        
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `SnackbarStyle with all parameters creates complete styling`() {
        val shape = RoundedCornerShape(12.dp)
        val elevation = 6.dp
        val messageStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        val actionStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        val style = SnackbarStyle(
            containerColor = Color(0xFF6200EE),
            contentColor = Color.White,
            actionColor = Color(0xFFBB86FC),
            shape = shape,
            elevation = elevation,
            messageTextStyle = messageStyle,
            actionTextStyle = actionStyle
        )
        
        assertEquals(Color(0xFF6200EE), style.containerColor)
        assertEquals(Color.White, style.contentColor)
        assertEquals(Color(0xFFBB86FC), style.actionColor)
        assertEquals(shape, style.shape)
        assertEquals(elevation, style.elevation)
        assertEquals(messageStyle, style.messageTextStyle)
        assertEquals(actionStyle, style.actionTextStyle)
    }

    @Test
    fun `SnackbarStyle with partial parameters uses defaults for others`() {
        val style = SnackbarStyle(
            containerColor = Color.Magenta,
            actionColor = Color.Cyan
        )
        
        assertEquals(Color.Magenta, style.containerColor)
        assertEquals(Color.Unspecified, style.contentColor) // Default
        assertEquals(Color.Cyan, style.actionColor)
        assertNull(style.shape) // Default
        assertNull(style.elevation) // Default
    }

    @Test
    fun `SnackbarStyle immutability through data class properties`() {
        val style = SnackbarStyle(
            containerColor = Color.Red,
            contentColor = Color.White
        )
        
        // Data class is immutable - creating new instances through copy
        val modifiedStyle = style.copy(containerColor = Color.Blue)
        
        assertEquals(Color.Red, style.containerColor) // Original unchanged
        assertEquals(Color.Blue, modifiedStyle.containerColor) // New instance changed
    }
}