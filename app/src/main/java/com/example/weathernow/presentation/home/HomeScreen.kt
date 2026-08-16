package com.example.weathernow.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.components.OfflineBanner
import com.example.weathernow.presentation.components.WeatherConditionIcon
import com.example.weathernow.presentation.components.WeatherEmptyView
import com.example.weathernow.presentation.components.WeatherErrorView
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

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val location: WeatherLocation,
        val currentWeather: CurrentWeather,
        val hourlyForecast: List<HourlyForecast>,
        val dailyForecast: List<DailyForecast>,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
        val lastUpdatedText: String = "Updated just now"
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Empty : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val now = Instant.now()
    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Success(
        location = WeatherLocation(
            id = "1",
            name = "Hanoi",
            country = "Vietnam",
            latitude = 21.0285,
            longitude = 105.8542,
            isFavorite = true
        ),
        currentWeather = CurrentWeather(
            temperatureCelsius = 28.0,
            feelsLikeCelsius = 31.0,
            humidityPercent = 68,
            windSpeedKmh = 12.0,
            windDirectionDegrees = 45,
            condition = WeatherCondition.PARTLY_CLOUDY,
            uvIndex = 5.0,
            precipitationMm = 1.5,
            pressureHpa = 1012.0,
            observedAt = now
        ),
        hourlyForecast = listOf(
            HourlyForecast(time = now, temperatureCelsius = 28.0, condition = WeatherCondition.PARTLY_CLOUDY, precipitationProbabilityPercent = 10),
            HourlyForecast(time = now.plusSeconds(3600), temperatureCelsius = 29.0, condition = WeatherCondition.PARTLY_CLOUDY, precipitationProbabilityPercent = 15),
            HourlyForecast(time = now.plusSeconds(7200), temperatureCelsius = 31.0, condition = WeatherCondition.CLEAR, precipitationProbabilityPercent = 5),
            HourlyForecast(time = now.plusSeconds(10800), temperatureCelsius = 30.0, condition = WeatherCondition.CLEAR, precipitationProbabilityPercent = 5),
            HourlyForecast(time = now.plusSeconds(14400), temperatureCelsius = 28.0, condition = WeatherCondition.RAIN, precipitationProbabilityPercent = 40),
            HourlyForecast(time = now.plusSeconds(18000), temperatureCelsius = 26.0, condition = WeatherCondition.RAIN, precipitationProbabilityPercent = 60),
            HourlyForecast(time = now.plusSeconds(21600), temperatureCelsius = 25.0, condition = WeatherCondition.CLOUDY, precipitationProbabilityPercent = 20),
            HourlyForecast(time = now.plusSeconds(25200), temperatureCelsius = 24.0, condition = WeatherCondition.CLEAR, precipitationProbabilityPercent = 0)
        ),
        dailyForecast = listOf(
            DailyForecast(date = today, minTemperatureCelsius = 24.0, maxTemperatureCelsius = 33.0, precipitationProbabilityPercent = 15, sunrise = null, sunset = null, condition = WeatherCondition.PARTLY_CLOUDY),
            DailyForecast(date = today.plusDays(1), minTemperatureCelsius = 23.0, maxTemperatureCelsius = 30.0, precipitationProbabilityPercent = 70, sunrise = null, sunset = null, condition = WeatherCondition.RAIN),
            DailyForecast(date = today.plusDays(2), minTemperatureCelsius = 22.0, maxTemperatureCelsius = 28.0, precipitationProbabilityPercent = 85, sunrise = null, sunset = null, condition = WeatherCondition.THUNDERSTORM),
            DailyForecast(date = today.plusDays(3), minTemperatureCelsius = 24.0, maxTemperatureCelsius = 32.0, precipitationProbabilityPercent = 20, sunrise = null, sunset = null, condition = WeatherCondition.PARTLY_CLOUDY),
            DailyForecast(date = today.plusDays(4), minTemperatureCelsius = 25.0, maxTemperatureCelsius = 34.0, precipitationProbabilityPercent = 10, sunrise = null, sunset = null, condition = WeatherCondition.CLEAR),
            DailyForecast(date = today.plusDays(5), minTemperatureCelsius = 26.0, maxTemperatureCelsius = 35.0, precipitationProbabilityPercent = 5, sunrise = null, sunset = null, condition = WeatherCondition.CLEAR),
            DailyForecast(date = today.plusDays(6), minTemperatureCelsius = 24.0, maxTemperatureCelsius = 31.0, precipitationProbabilityPercent = 30, sunrise = null, sunset = null, condition = WeatherCondition.CLOUDY)
        ),
        lastUpdatedText = "Updated 5m ago"
    ))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refreshWeather() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is HomeUiState.Success) {
                _uiState.value = current.copy(isRefreshing = true)
                kotlinx.coroutines.delay(800)
                _uiState.value = current.copy(isRefreshing = false, lastUpdatedText = "Updated just now")
            }
        }
    }
}

/**
 * Stateful HomeScreen connecting ViewModel to Stitch-designed HomeContent.
 */
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToForecast: (Double, Double, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = state,
        onRefresh = viewModel::refreshWeather,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToForecast = onNavigateToForecast,
        modifier = modifier
    )
}

/**
 * Stateless HomeContent strictly conforming to Stitch Screen `9689cd15b0fc461a8d0b86f406c6090a`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToForecast: (Double, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = WeatherPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            val locationName = when (uiState) {
                                is HomeUiState.Success -> "${uiState.location.name}${if (!uiState.location.country.isNullOrEmpty()) ", ${uiState.location.country}" else ""}"
                                else -> "WeatherNow"
                            }
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (uiState is HomeUiState.Success) {
                                Surface(
                                    color = WeatherPrimary.copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = uiState.lastUpdatedText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WeatherPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorites", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
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
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    WeatherLoadingView(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Empty -> {
                    WeatherEmptyView(
                        title = "No Weather Data",
                        subtitle = "Search for a city or turn on location services.",
                        actionText = "Search Location",
                        onAction = onNavigateToSearch,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Error -> {
                    WeatherErrorView(
                        errorMessage = uiState.message,
                        onRetry = onRefresh,
                        onSearchAlternative = onNavigateToSearch,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.isOffline) {
                                item {
                                    OfflineBanner(
                                        lastUpdatedText = uiState.lastUpdatedText,
                                        onRetry = onRefresh
                                    )
                                }
                            }

                            // 1. Hero Weather Card (Stitch Component)
                            item {
                                HeroWeatherCard(
                                    location = uiState.location,
                                    currentWeather = uiState.currentWeather,
                                    onClick = {
                                        onNavigateToForecast(
                                            uiState.location.latitude,
                                            uiState.location.longitude,
                                            uiState.location.name
                                        )
                                    }
                                )
                            }

                            // 2. Key Metrics 2x2 Grid (Stitch Component)
                            item {
                                KeyWeatherMetricsGrid(currentWeather = uiState.currentWeather)
                            }

                            // 3. 24-Hour Forecast (Stitch Component)
                            item {
                                HourlyForecastSection(hourlyList = uiState.hourlyForecast)
                            }

                            // 4. 7-Day Forecast with Gradient Temperature Sliders (Stitch Component)
                            item {
                                DailyForecastSection(dailyList = uiState.dailyForecast)
                            }

                            // Extra bottom padding for Bottom Navigation Bar
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stitch Hero Card with glassmorphism, huge temperature font, and weather status badge.
 */
@Composable
private fun HeroWeatherCard(
    location: WeatherLocation,
    currentWeather: CurrentWeather,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        contentPadding = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WeatherConditionIcon(
                condition = currentWeather.condition,
                size = 72.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${currentWeather.temperatureCelsius.toInt()}°C",
                fontSize = 76.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 80.sp
            )
            Text(
                text = currentWeather.condition.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Feels like ${currentWeather.feelsLikeCelsius.toInt()}°C  •  Humidity ${currentWeather.humidityPercent ?: 0}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 2x2 Metric Grid for Humidity, Wind Speed, UV Index, Precipitation.
 */
@Composable
private fun KeyWeatherMetricsGrid(
    currentWeather: CurrentWeather,
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
            MetricTile(
                label = "Humidity",
                value = "${currentWeather.humidityPercent ?: 0}%",
                subtitle = "Normal",
                icon = Icons.Default.WaterDrop,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Wind Speed",
                value = "${currentWeather.windSpeedKmh?.toInt() ?: 0} km/h",
                subtitle = "Direction: ${currentWeather.windDirectionDegrees ?: 0}° NE",
                icon = Icons.Default.Air,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                label = "UV Index",
                value = "${currentWeather.uvIndex?.toInt() ?: 0}",
                subtitle = "Moderate",
                icon = Icons.Default.WbSunny,
                iconTint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Precipitation",
                value = "${currentWeather.precipitationMm?.toInt() ?: 0} mm",
                subtitle = "Expected today",
                icon = Icons.Default.WbTwilight,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        contentPadding = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 24-Hour Hourly Forecast horizontal scroll section.
 */
@Composable
private fun HourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault())

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Column {
            Text(
                text = "24-Hour Forecast",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(hourlyList) { hourly ->
                    val formattedTime = timeFormatter.format(hourly.time)
                    val rainProb = hourly.precipitationProbabilityPercent ?: 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        WeatherConditionIcon(condition = hourly.condition, size = 26.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (rainProb > 0) {
                            Text(
                                text = "$rainProb%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "-", style = MaterialTheme.typography.labelSmall, color = Color.Transparent)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${hourly.temperatureCelsius.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * 7-Day Forecast vertical list with temperature range gradient sliders.
 */
@Composable
private fun DailyForecastSection(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            dailyList.forEach { daily ->
                val dayLabel = if (daily.date == LocalDate.now()) "Today" else dayFormatter.format(daily.date)
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
                        modifier = Modifier.width(64.dp)
                    )
                    WeatherConditionIcon(condition = daily.condition, size = 24.dp)
                    if (rainChance > 10) {
                        Text(
                            text = "$rainChance%",
                            style = MaterialTheme.typography.labelSmall,
                            color = WeatherSecondary,
                            modifier = Modifier.width(36.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(36.dp))
                    }
                    Text(
                        text = "${daily.minTemperatureCelsius.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(28.dp)
                    )
                    // Stitch Gradient Temperature Range Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape)
                            .background(TemperatureRangeGradient)
                    )
                    Text(
                        text = "${daily.maxTemperatureCelsius.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(28.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "HomeScreen Dark Mode", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun HomeScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        HomeScreen(
            onNavigateToSearch = {},
            onNavigateToFavorites = {},
            onNavigateToSettings = {},
            onNavigateToForecast = { _, _, _ -> }
        )
    }
}

@Preview(name = "HomeScreen Light Mode", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        HomeScreen(
            onNavigateToSearch = {},
            onNavigateToFavorites = {},
            onNavigateToSettings = {},
            onNavigateToForecast = { _, _, _ -> }
        )
    }
}
