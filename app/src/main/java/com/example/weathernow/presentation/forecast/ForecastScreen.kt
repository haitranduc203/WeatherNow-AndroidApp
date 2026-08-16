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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
    val adminArea: String? = null,
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
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false
)

class ForecastViewModel(
    private val weatherRepository: com.example.weathernow.domain.repository.WeatherRepository = com.example.weathernow.data.repository.WeatherRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForecastDetailUiState())
    val uiState: StateFlow<ForecastDetailUiState> = _uiState.asStateFlow()

    fun loadForecast(lat: Double, lon: Double, name: String, adminArea: String? = null) {
        viewModelScope.launch {
            val isFav = weatherRepository.isFavoriteLocation(lat, lon)
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                latitude = lat,
                longitude = lon,
                locationName = name,
                adminArea = adminArea,
                isFavorite = isFav
            )

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
                    isFavorite = isFav,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val state = _uiState.value
            val newFav = !state.isFavorite
            if (newFav) {
                val location = com.example.weathernow.domain.model.WeatherLocation(
                    id = "${String.format(java.util.Locale.US, "%.2f", state.latitude)}_${String.format(java.util.Locale.US, "%.2f", state.longitude)}",
                    name = state.locationName,
                    country = null,
                    adminArea = state.adminArea,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    isFavorite = true
                )
                weatherRepository.addFavoriteLocation(location)
            } else {
                val id = "${String.format(java.util.Locale.US, "%.2f", state.latitude)}_${String.format(java.util.Locale.US, "%.2f", state.longitude)}"
                weatherRepository.removeFavoriteLocation(id)
            }
            _uiState.value = _uiState.value.copy(isFavorite = newFav)
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
    adminArea: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(latitude, longitude, locationName, adminArea) {
        viewModel.loadForecast(latitude, longitude, locationName, adminArea)
    }

    val uiState by viewModel.uiState.collectAsState()

    ForecastContent(
        uiState = uiState,
        onRefresh = { viewModel.loadForecast(latitude, longitude, locationName, adminArea) },
        onToggleFavorite = viewModel::toggleFavorite,
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
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val displayTitle = if (uiState.adminArea.isNullOrBlank()) {
                        uiState.locationName
                    } else if (uiState.locationName.contains(uiState.adminArea)) {
                        uiState.locationName
                    } else {
                        "${uiState.locationName}, ${uiState.adminArea}"
                    }
                    Column {
                        Text(
                            text = displayTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.forecastSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        val displayTitle = if (uiState.adminArea.isNullOrBlank()) {
                            uiState.locationName
                        } else if (uiState.locationName.contains(uiState.adminArea)) {
                            uiState.locationName
                        } else {
                            "${uiState.locationName}, ${uiState.adminArea}"
                        }
                        val today = uiState.dailyList.firstOrNull()
                        val tempRange = if (today != null) " (${today.minTemperatureCelsius.toInt()}°C - ${today.maxTemperatureCelsius.toInt()}°C)" else ""
                        val shareText = buildString {
                            append("🌤️ Thời tiết tại $displayTitle:\n")
                            append("🌡️ Cảm giác như: ${uiState.feelsLikeCelsius.toInt()}°C$tempRange\n")
                            append("💧 Độ ẩm: ${uiState.humidityPercent}%\n")
                            append("💨 Gió: ${uiState.windGustsKmh.toInt()} km/h (Hướng ${uiState.windDirectionDegrees}°)\n")
                            append("☀️ Chỉ số UV: ${uiState.uvIndex.toInt()} | Áp suất: ${uiState.pressureHpa.toInt()} hPa\n")
                            append("🌅 Bình minh: ${uiState.sunriseTime} | 🌇 Hoàng hôn: ${uiState.sunsetTime}\n")
                            append("\n📲 Cập nhật từ ứng dụng WeatherNow")
                        }

                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            putExtra(android.content.Intent.EXTRA_TITLE, "Thời tiết $displayTitle")
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Chia sẻ thời tiết $displayTitle")
                        context.startActivity(shareIntent)
                    }) {
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
                com.example.weathernow.presentation.components.HomeScreenSkeleton(
                    modifier = Modifier.fillMaxSize()
                )
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
    val preferences by com.example.weathernow.presentation.settings.UserPreferencesRepository.preferencesFlow.collectAsState()

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
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Hardware-Accelerated Bézier Temperature Curve
            com.example.weathernow.presentation.components.TemperatureBezierChart(
                hourlyList = hourlyList,
                temperatureUnit = preferences.temperatureUnit
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Precipitation Probability & Volume Chart
            Text(
                text = strings.precipitation,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            com.example.weathernow.presentation.components.PrecipitationBarChart(
                hourlyList = hourlyList
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Hourly Horizontal Scroller
            Text(
                text = "Chi tiết theo giờ",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                        WeatherConditionIcon(
                            condition = hourly.condition,
                            isDay = hourly.isDay,
                            size = 28.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val formattedHourlyTemp = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatTemperature(
                            hourly.temperatureCelsius,
                            preferences.temperatureUnit,
                            includeUnitSymbol = true
                        )
                        Text(
                            text = formattedHourlyTemp,
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

    val preferences by com.example.weathernow.presentation.settings.UserPreferencesRepository.preferencesFlow.collectAsState()
    val formattedFeelsLike = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatTemperature(
        uiState.feelsLikeCelsius,
        preferences.temperatureUnit
    )
    val formattedWindGusts = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatWindSpeed(
        uiState.windGustsKmh,
        preferences.windSpeedUnit
    )

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
                value = formattedFeelsLike,
                subtitle = strings.thermalComfort,
                icon = Icons.Default.WbSunny,
                iconTint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            DetailedMetricTile(
                title = strings.windGusts,
                value = formattedWindGusts,
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
    modifier: Modifier = Modifier,
    currentTempCelsius: Double? = null
) {
    val currentLang = LocalAppLanguage.current
    val locale = if (currentLang == AppLanguage.VIETNAMESE) Locale.forLanguageTag("vi-VN") else Locale.ENGLISH
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
    val strings = LocalWeatherStrings.current
    val preferences by com.example.weathernow.presentation.settings.UserPreferencesRepository.preferencesFlow.collectAsState()

    val weekMin = dailyList.minOfOrNull { it.minTemperatureCelsius } ?: 20.0
    val weekMax = dailyList.maxOfOrNull { it.maxTemperatureCelsius } ?: 35.0

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
                val isToday = daily.date == LocalDate.now()
                val dayLabel = if (isToday) {
                    if (currentLang == AppLanguage.VIETNAMESE) "Hôm nay" else "Today"
                } else {
                    dayFormatter.format(daily.date).replaceFirstChar { it.uppercase() }
                }
                val rainChance = daily.precipitationProbabilityPercent ?: 0
                val minTempStr = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatTemperature(
                    daily.minTemperatureCelsius,
                    preferences.temperatureUnit,
                    includeUnitSymbol = false
                )
                val maxTempStr = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatTemperature(
                    daily.maxTemperatureCelsius,
                    preferences.temperatureUnit,
                    includeUnitSymbol = false
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(88.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(64.dp)
                    ) {
                        WeatherConditionIcon(condition = daily.condition, size = 22.dp)
                        if (rainChance > 10) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$rainChance%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Text(
                        text = minTempStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(30.dp)
                    )
                    // Modern Dynamic Temperature Range Bar (Apple Weather / M3 Style)
                    com.example.weathernow.presentation.components.TemperatureRangeBar(
                        minTemp = daily.minTemperatureCelsius,
                        maxTemp = daily.maxTemperatureCelsius,
                        weekMinTemp = weekMin,
                        weekMaxTemp = weekMax,
                        currentTemp = if (isToday) currentTempCelsius else null,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = maxTempStr,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(30.dp)
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
                onToggleFavorite = {},
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
                onToggleFavorite = {},
                onNavigateBack = {}
            )
        }
    }
}
