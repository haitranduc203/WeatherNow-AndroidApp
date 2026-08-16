package com.example.weathernow.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.weathernow.theme.GlassCardBackgroundDark
import com.example.weathernow.theme.GlassCardBackgroundLight
import com.example.weathernow.theme.GlassCardBorderDark
import com.example.weathernow.theme.GlassCardBorderLight
import com.example.weathernow.theme.WeatherNowTheme

/**
 * Reusable Glassmorphism Card Component synthesized from Google Stitch Design System.
 * Uses translucent backgrounds with a subtle high-contrast border and rounded corners.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val resolvedBackground = backgroundColor ?: if (isDark) GlassCardBackgroundDark else GlassCardBackgroundLight
    val resolvedBorder = borderColor ?: if (isDark) GlassCardBorderDark else GlassCardBorderLight

    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = resolvedBackground,
        border = BorderStroke(borderWidth, resolvedBorder)
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Preview(name = "GlassCard Light", showBackground = true)
@Composable
private fun GlassCardLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        GlassCard {
            androidx.compose.material3.Text("Glassmorphism Card Preview")
        }
    }
}

@Preview(name = "GlassCard Dark", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun GlassCardDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        GlassCard {
            androidx.compose.material3.Text(
                "Glassmorphism Card Preview",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
