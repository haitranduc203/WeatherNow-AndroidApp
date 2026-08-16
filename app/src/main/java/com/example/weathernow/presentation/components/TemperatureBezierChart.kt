package com.example.weathernow.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.presentation.util.WeatherUnitsFormatter
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * High-performance, hardware-accelerated Canvas Cubic Bézier Temperature Curve.
 * Supports fluid entry animation, vertical gradient fill, min/max guides, and touch scrubbing.
 */
@Composable
fun TemperatureBezierChart(
    hourlyList: List<HourlyForecast>,
    temperatureUnit: TemperatureUnit,
    modifier: Modifier = Modifier,
    lineColor: Color = WeatherPrimary,
    pointColor: Color = WeatherSecondary
) {
    if (hourlyList.isEmpty()) return

    val displayHours = hourlyList.take(24)
    val temps = displayHours.map { it.temperatureCelsius }
    val minTemp = temps.minOrNull() ?: 0.0
    val maxTemp = temps.maxOrNull() ?: 30.0
    val tempRange = max(1.0, maxTemp - minTemp)

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault())
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Entry animation progress (0f -> 1f)
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(hourlyList) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val minFormatted = WeatherUnitsFormatter.formatTemperature(minTemp, temperatureUnit)
    val maxFormatted = WeatherUnitsFormatter.formatTemperature(maxTemp, temperatureUnit)
    val chartDescription = "Biểu đồ nhiệt độ 24 giờ. Thấp nhất $minFormatted, cao nhất $maxFormatted."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = chartDescription }
    ) {
        // Scrubber Tooltip / Status Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeIndex = selectedIndex
            if (activeIndex != null && activeIndex in displayHours.indices) {
                val item = displayHours[activeIndex]
                val timeStr = timeFormatter.format(item.time)
                val tempStr = WeatherUnitsFormatter.formatTemperature(item.temperatureCelsius, temperatureUnit)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "$timeStr • $tempStr • ${item.condition.displayNameVi}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else {
                Text(
                    text = "Cao nhất: $maxFormatted  •  Thấp nhất: $minFormatted",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val step = size.width / (displayHours.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / step).toInt().coerceIn(0, displayHours.lastIndex)
                            selectedIndex = idx
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val step = size.width / (displayHours.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / step).toInt().coerceIn(0, displayHours.lastIndex)
                            selectedIndex = idx
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null },
                        onDrag = { change, _ ->
                            val step = size.width / (displayHours.size - 1).coerceAtLeast(1)
                            val idx = (change.position.x / step).toInt().coerceIn(0, displayHours.lastIndex)
                            selectedIndex = idx
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                val width = size.width
                val height = size.height
                val paddingVertical = 20.dp.toPx()
                val availableHeight = height - (paddingVertical * 2)

                val points = displayHours.mapIndexed { index, hour ->
                    val x = if (displayHours.size > 1) {
                        index * (width / (displayHours.size - 1))
                    } else {
                        width / 2f
                    }
                    val normalizedTemp = ((hour.temperatureCelsius - minTemp) / tempRange).toFloat()
                    // Apply entry animation progress
                    val animatedY = height - paddingVertical - (normalizedTemp * availableHeight * animProgress.value)
                    Offset(x, animatedY)
                }

                if (points.size >= 2) {
                    // 1. Build Smooth Cubic Bézier Curve Path
                    val curvePath = Path()
                    val fillPath = Path()

                    curvePath.moveTo(points[0].x, points[0].y)
                    fillPath.moveTo(points[0].x, height)
                    fillPath.lineTo(points[0].x, points[0].y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[max(0, i - 1)]
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val p3 = points[min(points.size - 1, i + 2)]

                        // Smooth Catmull-Rom spline control points
                        val controlPoint1 = Offset(
                            p1.x + (p2.x - p0.x) / 6f,
                            p1.y + (p2.y - p0.y) / 6f
                        )
                        val controlPoint2 = Offset(
                            p2.x - (p3.x - p1.x) / 6f,
                            p2.y - (p3.y - p1.y) / 6f
                        )

                        curvePath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p2.x, p2.y
                        )
                        fillPath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p2.x, p2.y
                        )
                    }

                    fillPath.lineTo(points.last().x, height)
                    fillPath.close()

                    // 2. Draw Gradient Under Curve
                    val gradientBrush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f * animProgress.value),
                            lineColor.copy(alpha = 0.05f * animProgress.value),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                    drawPath(fillPath, gradientBrush)

                    // 3. Draw Bézier Line Stroke
                    drawPath(
                        path = curvePath,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Draw Point Dots at intervals
                    val stepInterval = max(1, points.size / 8)
                    points.forEachIndexed { idx, pt ->
                        if (idx % stepInterval == 0 || idx == selectedIndex) {
                            val isSelected = idx == selectedIndex
                            val dotRadius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx()
                            val haloRadius = if (isSelected) 12.dp.toPx() else 0f

                            if (haloRadius > 0) {
                                drawCircle(
                                    color = lineColor.copy(alpha = 0.25f),
                                    radius = haloRadius,
                                    center = pt
                                )
                            }
                            drawCircle(
                                color = if (isSelected) Color.White else pointColor,
                                radius = dotRadius,
                                center = pt
                            )
                        }
                    }

                    // 5. Draw Selected Scrubber Vertical Guideline
                    val activeIdx = selectedIndex
                    if (activeIdx != null && activeIdx in points.indices) {
                        val activePoint = points[activeIdx]
                        drawLine(
                            color = lineColor.copy(alpha = 0.6f),
                            start = Offset(activePoint.x, 0f),
                            end = Offset(activePoint.x, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }
                }
            }
        }
    }
}
