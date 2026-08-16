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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.components.WeatherConditionIcon
import com.example.weathernow.presentation.components.WeatherLoadingView
import com.example.weathernow.presentation.util.LocalAppLanguage
import com.example.weathernow.presentation.util.LocalWeatherStrings
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.TemperatureRangeGradient
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ForecastDetailUiState(
    val locationName: String = "Tokyo",
    val latitude: Double = 35.6762,
    val longitude: Double = 139.6503,
    val hourlyList: List<HourlyForecast> = emptyList(),
    val dailyList: List<DailyForecast> = emptyList(),
    val feelsLikeCelsius: Double = 31.0,
    val windGustsKmh: Double = 22.5,
    val windDirectionDegrees: Int = 90,
    val uvIndex: Double = 6.0,
    val humidityPercent: Int = 72,
    val dewPointCelsius: Double = 22.0,
    val pressureHpa: Double = 1013.0,
    val visibilityKm: Double = 10.0,
    val sunriseTime: String = "05:42 AM",
    val sunsetTime: String = "18:28 PM",
    val solarNoonTime: String = "12:05 PM",
    val isLoading: Boolean = false
)

class ForecastViewModel(
    private val weatherRepository: com.example.weathernow.domain.repository.WeatherRepository = com.example.weathernow.data.repository.WeatherRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForecastDetailUiState())
    val uiState: StateFlow<ForecastDetailUiState> = _uiState.asStateFlow()

    fun loadForecast(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, latitude = lat, longitude = lon, locationName = name)

            try {
                var hourlyData: List<HourlyForecast> = emptyList()
                var dailyData: List<DailyForecast> = emptyList()
                var feelsLike = 28.0
                var windGusts = 15.0
                var windDir = 90
                var uv = 5.0
                var humidity = 65
                var pressure = 1013.0
                var sunriseStr = "05:35"
                var sunsetStr = "18:25"

                val currentRes = weatherRepository.observeCurrentWeather(lat, lon)
                    .first { it !is com.example.weathernow.core.common.Resource.Loading }
                if (currentRes is com.example.weathernow.core.common.Resource.Success) {
                    val current = currentRes.data
                    feelsLike = current.feelsLikeCelsius
                    windGusts = current.windSpeedKmh ?: 15.0
                    windDir = current.windDirectionDegrees ?: 90
                    uv = current.uvIndex ?: 5.0
                    humidity = current.humidityPercent ?: 65
                    pressure = current.pressureHpa ?: 1013.0
                }

                val hourlyRes = weatherRepository.observeHourlyForecast(lat, lon)
                    .first { it !is com.example.weathernow.core.common.Resource.Loading }
                if (hourlyRes is com.example.weathernow.core.common.Resource.Success) {
                    hourlyData = hourlyRes.data
                }

                val dailyRes = weatherRepository.observeDailyForecast(lat, lon)
                    .first { it !is com.example.weathernow.core.common.Resource.Loading }
                if (dailyRes is com.example.weathernow.core.common.Resource.Success) {
                    dailyData = dailyRes.data
                    val firstDay = dailyData.firstOrNull()
                    if (firstDay?.sunrise != null) {
                        sunriseStr = java.time.format.DateTimeFormatter.ofPattern("HH:mm").withZone(java.time.ZoneId.systemDefault()).format(firstDay.sunrise)
                    }
                    if (firstDay?.sunset != null) {
                        sunsetStr = java.time.format.DateTimeFormatter.ofPattern("HH:mm").withZone(java.time.ZoneId.systemDefault()).format(firstDay.sunset)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    hourlyList = hourlyData,
                    dailyList = dailyData,
                    feelsLikeCelsius = feelsLike,
                    windGustsKmh = windGusts,
                    windDirectionDegrees = windDir,
                    uvIndex = uv,
                    humidityPercent = humidity,
                    pressureHpa = pressure,
                    sunriseTime = sunriseStr,
                    sunsetTime = sunsetStr,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

/**
 * Stateful ForecastScreen.
 */
@Composable
fun ForecastScreen(
    latitude: Double,
    longitude: Double,
    locationName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(latitude, longitude, locationName) {
        viewModel.loadForecast(latitude, longitude, locationName)
    }

    val uiState by viewModel.uiState.collectAsState()

    ForecastContent(
        uiState = uiState,
        onRefresh = { viewModel.loadForecast(latitude, longitude, locationName) },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless ForecastContent conforming to Stitch Screen `14276268cb4a4c5f89bfbc8f79e5199b`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastContent(
    uiState: ForecastDetailUiState,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.forecastDetailTitle(uiState.locationName),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.weathernow.theme.atmosphericGradient())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                WeatherLoadingView(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
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
    val strings = LocalWeatherStrings.current
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
                    text = strings.forecast24h,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = CircleShape
                ) {
                    Text(
                        text = strings.liveTrend,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
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
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((32 * (rainProb / 100f)).dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$rainProb%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
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
    uiState: ForecastDetailUiState,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailedMetricTile(
                title = strings.feelsLikeLabel,
                value = "${uiState.feelsLikeCelsius.toInt()}°C",
                subtitle = strings.thermalComfort,
                icon = Icons.Default.WbSunny,
                iconTint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            DetailedMetricTile(
                title = strings.windGusts,
                value = "${uiState.windGustsKmh.toInt()} km/h",
                subtitle = strings.windDirectionLabel(uiState.windDirectionDegrees),
                icon = Icons.Default.Air,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailedMetricTile(
                title = strings.uvIndex,
                value = "${uiState.uvIndex.toInt()} ${strings.high}",
                subtitle = strings.moderateProtection,
                icon = Icons.Default.WbTwilight,
                iconTint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            DetailedMetricTile(
                title = strings.humidityDew,
                value = "${uiState.humidityPercent}%",
                subtitle = strings.dewPointLabel(uiState.dewPointCelsius.toInt()),
                icon = Icons.Default.WaterDrop,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailedMetricTile(
                title = strings.pressure,
                value = "${uiState.pressureHpa.toInt()} hPa",
                subtitle = strings.standardPressure,
                icon = Icons.Default.Compress,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            DetailedMetricTile(
                title = strings.visibility,
                value = "${uiState.visibilityKm.toInt()} km",
                subtitle = strings.clearAtmosphere,
                icon = Icons.Default.Visibility,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailedMetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Sun & Moon Horizon Cycle Arc Card.
 */
@Composable
private fun SolarCycleArcCard(
    sunrise: String,
    sunset: String,
    solarNoon: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 18.dp
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.sunMoonCycle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = strings.sunrise, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = sunrise, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = strings.solarNoon, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = solarNoon, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = strings.sunset, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = sunset, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/**
 * 7-Day Extended Forecast Detail Card.
 */
@Composable
private fun Extended7DayForecastCard(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    val currentLang = LocalAppLanguage.current
    val locale = if (currentLang == AppLanguage.VIETNAMESE) Locale("vi", "VN") else Locale.ENGLISH
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
    val strings = LocalWeatherStrings.current

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = strings.forecast7d,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            dailyList.forEach { daily ->
                val dayLabel = if (daily.date == LocalDate.now()) {
                    if (currentLang == AppLanguage.VIETNAMESE) "Hôm nay" else "Today"
                } else {
                    dayFormatter.format(daily.date).replaceFirstChar { it.uppercase() }
                }
                val rainChance = daily.precipitationProbabilityPercent ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(90.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(80.dp)
                    ) {
                        WeatherConditionIcon(condition = daily.condition, size = 22.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$rainChance%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    // Temperature Range Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape)
                            .background(TemperatureRangeGradient)
                    )
                    Text(
                        text = "${daily.minTemperatureCelsius.toInt()}° / ${daily.maxTemperatureCelsius.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(64.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Forecast Screen Dark", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun ForecastScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        ProvideWeatherLanguage(language = AppLanguage.VIETNAMESE) {
            ForecastContent(
                uiState = ForecastDetailUiState(),
                onRefresh = {},
                onNavigateBack = {}
            )
        }
    }
}

@Preview(name = "Forecast Screen Light", showBackground = true)
@Composable
private fun ForecastScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        ProvideWeatherLanguage(language = AppLanguage.ENGLISH) {
            ForecastContent(
                uiState = ForecastDetailUiState(),
                onRefresh = {},
                onNavigateBack = {}
            )
        }
    }
}
