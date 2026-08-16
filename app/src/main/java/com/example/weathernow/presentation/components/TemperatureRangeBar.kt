package com.example.weathernow.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern Dynamic Temperature Range Bar (Apple Weather / Material 3 Style).
 * Dynamically visualizes the daily temperature span relative to the week's min/max.
 * Includes an optional live glowing indicator dot for the current day's temperature.
 */
@Composable
fun TemperatureRangeBar(
    minTemp: Double,
    maxTemp: Double,
    weekMinTemp: Double,
    weekMaxTemp: Double,
    modifier: Modifier = Modifier,
    currentTemp: Double? = null,
    barHeight: Dp = 5.dp
) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val dotBorderColor = MaterialTheme.colorScheme.surface
    val dotFillColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier.height(barHeight + 6.dp)
    ) {
        val totalRange = (weekMaxTemp - weekMinTemp).coerceAtLeast(1.0)
        val h = barHeight.toPx()
        val topY = (size.height - h) / 2f
        val centerY = size.height / 2f

        // 1. Draw subtle background track
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, topY),
            size = Size(size.width, h),
            cornerRadius = CornerRadius(h / 2f, h / 2f)
        )

        // 2. Compute dynamic start and end X coordinates
        val startRatio = ((minTemp - weekMinTemp) / totalRange).coerceIn(0.0, 1.0).toFloat()
        val endRatio = ((maxTemp - weekMinTemp) / totalRange).coerceIn(0.0, 1.0).toFloat()

        val startX = startRatio * size.width
        val endX = (endRatio * size.width).coerceAtLeast(startX + h)
        val pillWidth = endX - startX

        // 3. Draw active range pill with tailored temperature gradient
        val rangeGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF38BDF8), // Cyan / Sky blue
                Color(0xFFFB923C), // Warm Amber
                Color(0xFFEF4444)  // Coral Red
            ),
            startX = startX,
            endX = endX
        )

        drawRoundRect(
            brush = rangeGradient,
            topLeft = Offset(startX, topY),
            size = Size(pillWidth, h),
            cornerRadius = CornerRadius(h / 2f, h / 2f)
        )

        // 4. Draw glowing current temperature dot indicator (if today)
        if (currentTemp != null) {
            val currentRatio = ((currentTemp - weekMinTemp) / totalRange).coerceIn(0.0, 1.0).toFloat()
            val dotX = (currentRatio * size.width).coerceIn(startX, endX)

            // Outer border / glow
            drawCircle(
                color = dotBorderColor,
                radius = 4.5.dp.toPx(),
                center = Offset(dotX, centerY)
            )
            // Inner dot
            drawCircle(
                color = dotFillColor,
                radius = 2.8.dp.toPx(),
                center = Offset(dotX, centerY)
            )
        }
    }
}
