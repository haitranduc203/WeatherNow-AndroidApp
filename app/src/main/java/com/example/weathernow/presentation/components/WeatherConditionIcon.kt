package com.example.weathernow.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherSecondary
import com.example.weathernow.theme.WeatherTertiary

/**
 * Dynamic Weather Condition Icon with colorful tints matching Stitch style.
 */
@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color? = null
) {
    val (icon, defaultTint) = when (condition) {
        WeatherCondition.CLEAR -> Icons.Default.WbSunny to WeatherTertiary
        WeatherCondition.PARTLY_CLOUDY -> Icons.Default.WbCloudy to Color(0xFFFFCA28)
        WeatherCondition.CLOUDY -> Icons.Default.Cloud to Color(0xFF90A4AE)
        WeatherCondition.FOG -> Icons.Default.Air to Color(0xFFB0BEC5)
        WeatherCondition.DRIZZLE,
        WeatherCondition.RAIN -> Icons.Default.Grain to WeatherSecondary
        WeatherCondition.THUNDERSTORM -> Icons.Default.Thunderstorm to Color(0xFFFFD54F)
        WeatherCondition.SNOW -> Icons.Default.AcUnit to Color(0xFFE1F5FE)
        WeatherCondition.UNKNOWN -> Icons.Default.Cloud to Color(0xFF78909C)
    }

    Icon(
        imageVector = icon,
        contentDescription = condition.displayName,
        tint = tint ?: defaultTint,
        modifier = modifier.size(size)
    )
}

@Preview(name = "Weather Icons Preview", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun WeatherConditionIconPreview() {
    WeatherNowTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            WeatherConditionIcon(condition = WeatherCondition.CLEAR)
            WeatherConditionIcon(condition = WeatherCondition.PARTLY_CLOUDY)
            WeatherConditionIcon(condition = WeatherCondition.RAIN)
            WeatherConditionIcon(condition = WeatherCondition.THUNDERSTORM)
            WeatherConditionIcon(condition = WeatherCondition.SNOW)
        }
    }
}
