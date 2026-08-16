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
import com.example.weathernow.domain.model.AppLanguage
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
import com.example.weathernow.presentation.util.LocalAppLanguage
import com.example.weathernow.presentation.util.LocalWeatherStrings
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.TemperatureRangeGradient
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import kotlinx.coroutines.delay
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

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Success(
        val location: WeatherLocation,
        val currentWeather: CurrentWeather,
        val hourlyForecast: List<HourlyForecast> = emptyList(),
        val dailyForecast: List<DailyForecast> = emptyList(),
        val isOffline: Boolean = false,
        val lastUpdatedText: String? = null,
        val isRefreshing: Boolean = false
    ) : HomeUiState
}

class HomeViewModel(
    private val weatherRepository: com.example.weathernow.domain.repository.WeatherRepository = com.example.weathernow.WeatherNowApp.instance?.appContainer?.weatherRepository ?: com.example.weathernow.data.repository.WeatherRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val favoriteLocationsFlow = weatherRepository.observeFavoriteLocations()

    init {
        loadWeatherData()
    }

    fun loadWeatherData(
        latitude: Double = 21.0285,
        longitude: Double = 105.8542,
        locationName: String = "Hà Nội",
        adminArea: String? = "Thủ đô Hà Nội",
        country: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                weatherRepository.observeCurrentWeather(latitude, longitude).collect { currentRes ->
                    when (currentRes) {
                        is com.example.weathernow.core.common.Resource.Loading -> {
                            if (_uiState.value !is HomeUiState.Success) {
                                _uiState.value = HomeUiState.Loading
                            }
                        }
                        is com.example.weathernow.core.common.Resource.Success -> {
                            val currentWeather = currentRes.data
                            val location = WeatherLocation(
                                id = "loc_${latitude}_${longitude}",
                                name = locationName,
                                country = country ?: (if (locationName == "Hà Nội") "Việt Nam" else null),
                                adminArea = adminArea,
                                latitude = latitude,
                                longitude = longitude
                            )

                            var hourlyData: List<HourlyForecast> = emptyList()
                            var dailyData: List<DailyForecast> = emptyList()

                            try {
                                val hourlyRes = weatherRepository.observeHourlyForecast(latitude, longitude)
                                    .first { it !is com.example.weathernow.core.common.Resource.Loading }
                                if (hourlyRes is com.example.weathernow.core.common.Resource.Success) {
                                    hourlyData = hourlyRes.data
                                }

                                val dailyRes = weatherRepository.observeDailyForecast(latitude, longitude)
                                    .first { it !is com.example.weathernow.core.common.Resource.Loading }
                                if (dailyRes is com.example.weathernow.core.common.Resource.Success) {
                                    dailyData = dailyRes.data
                                }
                            } catch (_: Exception) {}

                            com.example.weathernow.domain.model.ActiveLocationManager.setActiveLocation(location)

                            _uiState.value = HomeUiState.Success(
                                location = location,
                                currentWeather = currentWeather,
                                hourlyForecast = hourlyData,
                                dailyForecast = dailyData,
                                isOffline = false,
                                lastUpdatedText = null,
                                isRefreshing = false
                            )
                        }
                        is com.example.weathernow.core.common.Resource.Error -> {
                            _uiState.value = HomeUiState.Error(currentRes.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load live weather")
            }
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        val lat = if (currentState is HomeUiState.Success) currentState.location.latitude else 21.0285
        val lon = if (currentState is HomeUiState.Success) currentState.location.longitude else 105.8542
        val name = if (currentState is HomeUiState.Success) currentState.location.name else "Hà Nội"
        val admin = if (currentState is HomeUiState.Success) currentState.location.adminArea else "Thủ đô Hà Nội"
        val country = if (currentState is HomeUiState.Success) currentState.location.country else "Việt Nam"
        loadWeatherData(lat, lon, name, admin, country)
    }
}

/**
 * Stateful HomeScreen.
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
    val uiState by viewModel.uiState.collectAsState()
    val favoriteLocations by viewModel.favoriteLocationsFlow.collectAsState(initial = emptyList())

    HomeContent(
        uiState = uiState,
        favoriteLocations = favoriteLocations,
        onRefresh = viewModel::refresh,
        onSelectLocation = { lat, lon, name, admin, country ->
            viewModel.loadWeatherData(lat, lon, name, admin, country)
        },
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToForecast = onNavigateToForecast,
        modifier = modifier
    )
}

/**
 * Stateless HomeContent conforming to Stitch Screen `9689cd15b0fc461a8d0b86f406c6090a`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    favoriteLocations: List<WeatherLocation> = emptyList(),
    onRefresh: () -> Unit,
    onSelectLocation: (Double, Double, String, String?, String?) -> Unit = { _, _, _, _, _ -> },
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToForecast: (Double, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current
    var showLocationSheet by remember { mutableStateOf(false) }

    if (showLocationSheet) {
        val currentLocName = (uiState as? HomeUiState.Success)?.location?.name ?: "Hà Nội"
        LocationSwitcherBottomSheet(
            currentLocationName = currentLocName,
            favoriteLocations = favoriteLocations,
            onSelectLocation = onSelectLocation,
            onNavigateToSearch = onNavigateToSearch,
            onDismiss = { showLocationSheet = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is HomeUiState.Success) {
                        Surface(
                            onClick = { showLocationSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    val country = uiState.location.country
                                    val locationTitle = if (country.isNullOrBlank() || uiState.location.name.contains(country)) {
                                        uiState.location.name
                                    } else {
                                        "${uiState.location.name}, $country"
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = locationTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Đổi tỉnh thành",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        shape = CircleShape,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = uiState.lastUpdatedText ?: strings.updatedJustNow,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
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
                    com.example.weathernow.presentation.components.HomeScreenSkeleton(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is HomeUiState.Empty -> {
                    WeatherEmptyView(
                        title = strings.noWeatherDataTitle,
                        subtitle = strings.noWeatherDataSubtitle,
                        actionText = strings.searchLocation,
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
                                        lastUpdatedText = uiState.lastUpdatedText ?: strings.updatedJustNow,
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

                            // 4. 7-Day Forecast with Dynamic Temperature Sliders (Stitch Component)
                            item {
                                DailyForecastSection(
                                    dailyList = uiState.dailyForecast,
                                    currentTempCelsius = uiState.currentWeather.temperatureCelsius
                                )
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
    val strings = LocalWeatherStrings.current
    val currentLang = LocalAppLanguage.current
    val preferences by com.example.weathernow.presentation.settings.UserPreferencesRepository.preferencesFlow.collectAsState()
    val formattedTemp = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatTemperature(
        currentWeather.temperatureCelsius,
        preferences.temperatureUnit
    )
    val formattedFeelsLike = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatTemperature(
        currentWeather.feelsLikeCelsius,
        preferences.temperatureUnit
    )

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
                isDay = currentWeather.isDay,
                size = 72.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formattedTemp,
                fontSize = 76.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 80.sp
            )
            Text(
                text = currentWeather.condition.localizedName(currentLang),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${strings.feelsLikeLabel} $formattedFeelsLike  •  ${strings.humidity} ${currentWeather.humidityPercent ?: 0}%",
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
    val strings = LocalWeatherStrings.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val preferences by com.example.weathernow.presentation.settings.UserPreferencesRepository.preferencesFlow.collectAsState()
            val formattedWind = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatWindSpeed(
                currentWeather.windSpeedKmh ?: 0.0,
                preferences.windSpeedUnit
            )
            MetricTile(
                label = strings.humidity,
                value = "${currentWeather.humidityPercent ?: 0}%",
                subtitle = strings.normal,
                icon = Icons.Default.WaterDrop,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = strings.windSpeed,
                value = formattedWind,
                subtitle = "NE ${currentWeather.windDirectionDegrees ?: 0}°",
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
                label = strings.uvIndex,
                value = "${currentWeather.uvIndex?.toInt() ?: 0}",
                subtitle = strings.moderate,
                icon = Icons.Default.WbSunny,
                iconTint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = strings.precipitation,
                value = "${currentWeather.precipitationMm?.toInt() ?: 0} mm",
                subtitle = strings.expectedToday,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 24-Hour Forecast horizontal scroll strip.
 */
@Composable
private fun HourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
    val preferences by com.example.weathernow.presentation.settings.UserPreferencesRepository.preferencesFlow.collectAsState()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Column {
            Text(
                text = strings.forecast24h,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(hourlyList) { hourly ->
                    val formattedTime = timeFormatter.format(hourly.time)
                    val rainProb = hourly.precipitationProbabilityPercent ?: 0
                    val tempStr = com.example.weathernow.presentation.util.WeatherUnitsFormatter.formatTemperature(
                        hourly.temperatureCelsius,
                        preferences.temperatureUnit,
                        includeUnitSymbol = false
                    )
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
                        WeatherConditionIcon(
                            condition = hourly.condition,
                            isDay = hourly.isDay,
                            size = 26.dp
                        )
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
                            text = tempStr,
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
 * 7-Day Forecast vertical list with dynamic temperature range gradient bars.
 */
@Composable
private fun DailyForecastSection(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier,
    currentTempCelsius: Double? = null
) {
    val currentLang = LocalAppLanguage.current
    val locale = if (currentLang == AppLanguage.VIETNAMESE) Locale.forLanguageTag("vi-VN") else Locale.ENGLISH
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", locale)
    val strings = LocalWeatherStrings.current
    val preferences by com.example.weathernow.presentation.settings.UserPreferencesRepository.preferencesFlow.collectAsState()

    val weekMin = dailyList.minOfOrNull { it.minTemperatureCelsius } ?: 20.0
    val weekMax = dailyList.maxOfOrNull { it.maxTemperatureCelsius } ?: 35.0

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = strings.forecast7d,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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
                        modifier = Modifier.width(76.dp)
                    )
                    WeatherConditionIcon(condition = daily.condition, size = 24.dp)
                    if (rainChance > 10) {
                        Text(
                            text = "$rainChance%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.width(36.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(36.dp))
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

/**
 * Modern modal bottom sheet for switching active city/province on HomeScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSwitcherBottomSheet(
    currentLocationName: String,
    favoriteLocations: List<WeatherLocation>,
    onSelectLocation: (Double, Double, String, String?, String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đổi tỉnh / thành phố",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Option 1: Default / Current Location (Hanoi)
            val isCurrentSelected = currentLocationName == "Hà Nội"
            Surface(
                onClick = {
                    onSelectLocation(21.0285, 105.8542, "Hà Nội", "Thủ đô Hà Nội", "Việt Nam")
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                color = if (isCurrentSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hà Nội (Mặc định)",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Việt Nam",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isCurrentSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (favoriteLocations.isNotEmpty()) {
                Text(
                    text = "Địa điểm yêu thích (${favoriteLocations.size})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteLocations) { loc ->
                        val isSelected = currentLocationName == loc.name
                        Surface(
                            onClick = {
                                onSelectLocation(loc.latitude, loc.longitude, loc.name, loc.adminArea, loc.country)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = loc.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val subtitle = if (loc.adminArea != null && loc.country != null && !loc.adminArea.contains(loc.country)) {
                                        "${loc.adminArea}, ${loc.country}"
                                    } else {
                                        loc.adminArea ?: loc.country ?: ""
                                    }
                                    if (subtitle.isNotBlank()) {
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Option 3: Search new city
            Surface(
                onClick = {
                    onDismiss()
                    onNavigateToSearch()
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tìm kiếm tỉnh / thành phố khác",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
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
        ProvideWeatherLanguage(language = AppLanguage.VIETNAMESE) {
            HomeScreen(
                onNavigateToSearch = {},
                onNavigateToFavorites = {},
                onNavigateToSettings = {},
                onNavigateToForecast = { _, _, _ -> }
            )
        }
    }
}

@Preview(name = "HomeScreen Light Mode", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        ProvideWeatherLanguage(language = AppLanguage.ENGLISH) {
            HomeScreen(
                onNavigateToSearch = {},
                onNavigateToFavorites = {},
                onNavigateToSettings = {},
                onNavigateToForecast = { _, _, _ -> }
            )
        }
    }
}
