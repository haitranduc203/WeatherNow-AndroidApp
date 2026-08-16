package com.example.weathernow.presentation.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.components.WeatherConditionIcon
import com.example.weathernow.presentation.components.WeatherLoadingView
import com.example.weathernow.theme.AtmosphericGradientDark
import com.example.weathernow.theme.TemperatureRangeGradient
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherSecondary
import com.example.weathernow.theme.WeatherTertiary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ForecastUiState(
    val locationName: String = "Hanoi",
    val currentWeather: CurrentWeather = CurrentWeather(
        temperatureCelsius = 28.0,
        feelsLikeCelsius = 31.0,
        humidityPercent = 72,
        windSpeedKmh = 14.0,
        windDirectionDegrees = 90,
        condition = WeatherCondition.PARTLY_CLOUDY,
        uvIndex = 6.0,
        precipitationMm = 1.2,
        pressureHpa = 1013.0,
        observedAt = Instant.now()
    ),
    val hourlyList: List<HourlyForecast> = listOf(
        HourlyForecast(time = Instant.now(), temperatureCelsius = 24.0, condition = WeatherCondition.CLEAR, precipitationProbabilityPercent = 0),
        HourlyForecast(time = Instant.now().plusSeconds(10800), temperatureCelsius = 27.0, condition = WeatherCondition.PARTLY_CLOUDY, precipitationProbabilityPercent = 5),
        HourlyForecast(time = Instant.now().plusSeconds(21600), temperatureCelsius = 32.0, condition = WeatherCondition.PARTLY_CLOUDY, precipitationProbabilityPercent = 10),
        HourlyForecast(time = Instant.now().plusSeconds(32400), temperatureCelsius = 31.0, condition = WeatherCondition.RAIN, precipitationProbabilityPercent = 45),
        HourlyForecast(time = Instant.now().plusSeconds(43200), temperatureCelsius = 27.0, condition = WeatherCondition.THUNDERSTORM, precipitationProbabilityPercent = 80),
        HourlyForecast(time = Instant.now().plusSeconds(54000), temperatureCelsius = 25.0, condition = WeatherCondition.RAIN, precipitationProbabilityPercent = 60),
        HourlyForecast(time = Instant.now().plusSeconds(64800), temperatureCelsius = 24.0, condition = WeatherCondition.CLOUDY, precipitationProbabilityPercent = 20)
    ),
    val dailyList: List<DailyForecast> = listOf(
        DailyForecast(date = LocalDate.now(), minTemperatureCelsius = 24.0, maxTemperatureCelsius = 33.0, precipitationProbabilityPercent = 45, sunrise = null, sunset = null, condition = WeatherCondition.PARTLY_CLOUDY),
        DailyForecast(date = LocalDate.now().plusDays(1), minTemperatureCelsius = 23.0, maxTemperatureCelsius = 30.0, precipitationProbabilityPercent = 75, sunrise = null, sunset = null, condition = WeatherCondition.RAIN),
        DailyForecast(date = LocalDate.now().plusDays(2), minTemperatureCelsius = 22.0, maxTemperatureCelsius = 28.0, precipitationProbabilityPercent = 90, sunrise = null, sunset = null, condition = WeatherCondition.THUNDERSTORM),
        DailyForecast(date = LocalDate.now().plusDays(3), minTemperatureCelsius = 24.0, maxTemperatureCelsius = 32.0, precipitationProbabilityPercent = 30, sunrise = null, sunset = null, condition = WeatherCondition.PARTLY_CLOUDY),
        DailyForecast(date = LocalDate.now().plusDays(4), minTemperatureCelsius = 25.0, maxTemperatureCelsius = 34.0, precipitationProbabilityPercent = 10, sunrise = null, sunset = null, condition = WeatherCondition.CLEAR),
        DailyForecast(date = LocalDate.now().plusDays(5), minTemperatureCelsius = 26.0, maxTemperatureCelsius = 35.0, precipitationProbabilityPercent = 5, sunrise = null, sunset = null, condition = WeatherCondition.CLEAR),
        DailyForecast(date = LocalDate.now().plusDays(6), minTemperatureCelsius = 24.0, maxTemperatureCelsius = 31.0, precipitationProbabilityPercent = 20, sunrise = null, sunset = null, condition = WeatherCondition.CLOUDY)
    ),
    val sunriseTime: String = "05:42 AM",
    val sunsetTime: String = "18:28 PM",
    val solarNoonTime: String = "12:05 PM",
    val visibilityKm: Double = 10.0,
    val dewPointCelsius: Double = 22.0,
    val isLoading: Boolean = false
)

class ForecastViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            kotlinx.coroutines.delay(600)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}

/**
 * Stateful ForecastScreen.
 */
@Composable
fun ForecastScreen(
    locationName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    ForecastContent(
        uiState = state.copy(locationName = locationName),
        onRefresh = viewModel::refresh,
        onShare = { /* Share weather summary */ },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless ForecastContent strictly conforming to Stitch Screen `14276268cb4a4c5f89bfbc8f79e5199b`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastContent(
    uiState: ForecastUiState,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("${uiState.locationName} — Forecast & Trends", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WeatherPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AtmosphericGradientDark)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                WeatherLoadingView(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    // 1. 24-Hour Trend Visual Card (Stitch Component)
                    item {
                        HourlyTrendChartCard(hourlyList = uiState.hourlyList)
                    }

                    // 2. 2-Column Detailed Weather Parameters Grid (Stitch Component)
                    item {
                        DetailedMetricsGrid(uiState = uiState)
                    }

                    // 3. Sun & Moon Horizon Cycle Card (Stitch Component)
                    item {
                        SolarCycleArcCard(
                            sunrise = uiState.sunriseTime,
                            sunset = uiState.sunsetTime,
                            solarNoon = uiState.solarNoonTime
                        )
                    }

                    // 4. 7-Day Extended Forecast Detail (Stitch Component)
                    item {
                        Extended7DayForecastCard(dailyList = uiState.dailyList)
                    }
                }
            }
        }
    }
}

/**
 * Hourly trend chart card with temperature timeline and precipitation bars.
 */
@Composable
private fun HourlyTrendChartCard(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault())

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 18.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "24-Hour Temperature & Rain Trend",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = WeatherSecondary.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Live Trend",
                        style = MaterialTheme.typography.labelSmall,
                        color = WeatherSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(hourlyList) { hourly ->
                    val rainProb = hourly.precipitationProbabilityPercent ?: 0
                    val formattedTime = timeFormatter.format(hourly.time)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        WeatherConditionIcon(condition = hourly.condition, size = 28.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${hourly.temperatureCelsius.toInt()}°C",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Precipitation bar
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((32 * (rainProb / 100f)).dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(WeatherSecondary)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$rainProb%",
                            style = MaterialTheme.typography.labelSmall,
                            color = WeatherSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2-Column detailed atmospheric metrics grid.
 */
@Composable
private fun DetailedMetricsGrid(
    uiState: ForecastUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailMetricTile(
                title = "Feels Like",
                value = "${uiState.currentWeather.feelsLikeCelsius.toInt()}°C",
                description = "Humidity makes it feel warmer",
                icon = Icons.Default.WbSunny,
                iconTint = WeatherTertiary,
                modifier = Modifier.weight(1f)
            )
            DetailMetricTile(
                title = "Wind & Gusts",
                value = "${uiState.currentWeather.windSpeedKmh?.toInt() ?: 0} km/h",
                description = "Direction: ${uiState.currentWeather.windDirectionDegrees ?: 0}° East",
                icon = Icons.Default.Air,
                iconTint = WeatherPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailMetricTile(
                title = "UV Index",
                value = "${uiState.currentWeather.uvIndex?.toInt() ?: 0} High",
                description = "Protection required until 16:00",
                icon = Icons.Default.WbTwilight,
                iconTint = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f)
            )
            DetailMetricTile(
                title = "Humidity & Dew",
                value = "${uiState.currentWeather.humidityPercent ?: 0}%",
                description = "The dew point is ${uiState.dewPointCelsius.toInt()}°C",
                icon = Icons.Default.WaterDrop,
                iconTint = WeatherSecondary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailMetricTile(
                title = "Pressure",
                value = "${uiState.currentWeather.pressureHpa?.toInt() ?: 1013} hPa",
                description = "Standard atmospheric pressure",
                icon = Icons.Default.Compress,
                iconTint = Color(0xFF81C784),
                modifier = Modifier.weight(1f)
            )
            DetailMetricTile(
                title = "Visibility",
                value = "${uiState.visibilityKm.toInt()} km",
                description = "Clear atmospheric view",
                icon = Icons.Default.Visibility,
                iconTint = Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailMetricTile(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        contentPadding = 14.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Solar cycle arc card showing sunrise, solar noon, and sunset.
 */
@Composable
private fun SolarCycleArcCard(
    sunrise: String,
    sunset: String,
    solarNoon: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = WeatherTertiary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sun & Moon Horizon Cycle",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "Sunrise", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = sunrise, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Solar Noon", style = MaterialTheme.typography.labelSmall, color = WeatherTertiary)
                    Text(text = solarNoon, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = WeatherTertiary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Sunset", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = sunset, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/**
 * 7-Day extended daily forecast breakdown.
 */
@Composable
private fun Extended7DayForecastCard(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Extended 7-Day Forecast",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            dailyList.forEach { daily ->
                val dayText = if (daily.date == LocalDate.now()) "Today" else dayFormatter.format(daily.date)
                val rainChance = daily.precipitationProbabilityPercent ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dayText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(80.dp)
                    )
                    WeatherConditionIcon(condition = daily.condition, size = 22.dp)
                    Text(
                        text = "$rainChance% rain",
                        style = MaterialTheme.typography.labelSmall,
                        color = WeatherSecondary,
                        modifier = Modifier.width(56.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .padding(horizontal = 6.dp)
                            .clip(CircleShape)
                            .background(TemperatureRangeGradient)
                    )
                    Text(
                        text = "${daily.minTemperatureCelsius.toInt()}° / ${daily.maxTemperatureCelsius.toInt()}°",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "ForecastScreen Dark Mode", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun ForecastScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        ForecastScreen(
            locationName = "Hanoi",
            onNavigateBack = {}
        )
    }
}

@Preview(name = "ForecastScreen Light Mode", showBackground = true)
@Composable
private fun ForecastScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        ForecastScreen(
            locationName = "Hanoi",
            onNavigateBack = {}
        )
    }
}
