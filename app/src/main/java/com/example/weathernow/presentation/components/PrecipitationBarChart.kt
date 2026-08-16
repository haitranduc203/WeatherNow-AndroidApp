package com.example.weathernow.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.domain.model.HourlyForecast
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 24-hour precipitation probability and volume bar chart with animated entrance.
 */
@Composable
fun PrecipitationBarChart(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    val displayHours = hourlyList.take(24)
    val maxProb = displayHours.maxOfOrNull { it.precipitationProbabilityPercent ?: 0 } ?: 0

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault())
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(hourlyList) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val chartDescription = "Biểu đồ xác suất mưa 24 giờ. Cao nhất $maxProb%."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = chartDescription }
    ) {
        // Status row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeIdx = selectedIndex
            if (activeIdx != null && activeIdx in displayHours.indices) {
                val item = displayHours[activeIdx]
                val timeStr = timeFormatter.format(item.time)
                val prob = item.precipitationProbabilityPercent ?: 0
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "$timeStr • Khả năng mưa $prob%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else {
                Text(
                    text = "Xác suất mưa cao nhất hôm nay: $maxProb%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val step = size.width / displayHours.size
                        val idx = (offset.x / step).toInt().coerceIn(0, displayHours.lastIndex)
                        selectedIndex = idx
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val width = size.width
                val height = size.height
                val barCount = displayHours.size
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (barCount - 1)
                val barWidth = (width - totalSpacing) / barCount

                displayHours.forEachIndexed { index, hour ->
                    val prob = (hour.precipitationProbabilityPercent ?: 0).coerceIn(0, 100)
                    val normalizedHeight = (prob / 100f) * (height - 10.dp.toPx()) * animProgress.value
                    val barHeight = maxOf(4.dp.toPx(), normalizedHeight)
                    val x = index * (barWidth + barSpacing)
                    val y = height - barHeight

                    val isSelected = index == selectedIndex
                    val gradient = Brush.verticalGradient(
                        colors = if (isSelected) {
                            listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))
                        } else {
                            listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.85f),
                                Color(0xFF0284C7).copy(alpha = 0.55f)
                            )
                        },
                        startY = y,
                        endY = height
                    )

                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }
    }
}
