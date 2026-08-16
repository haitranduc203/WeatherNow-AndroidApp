package com.example.weathernow.presentation.favorites

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.ActiveLocationManager
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.components.WeatherConditionIcon
import com.example.weathernow.presentation.components.WeatherEmptyView
import com.example.weathernow.presentation.components.WeatherLoadingView
import com.example.weathernow.presentation.util.LocalAppLanguage
import com.example.weathernow.presentation.util.LocalWeatherStrings
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.WeatherNowTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class FavoriteItemUiModel(
    val location: WeatherLocation,
    val temperature: Double,
    val condition: WeatherCondition,
    val localTime: String,
    val minTemp: Double,
    val maxTemp: Double
)

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Success(
        val currentLocation: FavoriteItemUiModel? = null,
        val favoritesList: List<FavoriteItemUiModel> = emptyList()
    ) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}

class FavoritesViewModel(
    private val weatherRepository: com.example.weathernow.domain.repository.WeatherRepository = com.example.weathernow.data.repository.WeatherRepositoryImpl(),
    private val activeLocationManager: ActiveLocationManager = ActiveLocationManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            combine(
                activeLocationManager.activeLocation,
                weatherRepository.observeFavoriteLocations()
            ) { activeLoc, favLocations ->
                // 1. Build current active location UI model instantly from active location
                val tz = try {
                    if (!activeLoc.timezone.isNullOrBlank()) java.time.ZoneId.of(activeLoc.timezone) else java.time.ZoneId.systemDefault()
                } catch (_: Exception) {
                    java.time.ZoneId.systemDefault()
                }
                val localTimeFormatted = java.time.ZonedDateTime.now(tz).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))

                val currentActiveModel = FavoriteItemUiModel(
                    location = activeLoc,
                    temperature = when (activeLoc.name) {
                        "Tokyo" -> 19.0
                        "Paris" -> 22.0
                        "New York" -> 16.0
                        "Sydney" -> 24.0
                        "Hà Nội", "Thái Bình", "Hưng Yên" -> 28.0
                        else -> 26.0
                    },
                    condition = when (activeLoc.name) {
                        "Paris" -> WeatherCondition.RAIN
                        "New York" -> WeatherCondition.CLOUDY
                        else -> WeatherCondition.CLEAR
                    },
                    localTime = localTimeFormatted,
                    minTemp = when (activeLoc.name) {
                        "Paris" -> 16.0
                        "Tokyo" -> 14.0
                        "New York" -> 12.0
                        else -> 24.0
                    },
                    maxTemp = when (activeLoc.name) {
                        "Paris" -> 26.0
                        "Tokyo" -> 24.0
                        "New York" -> 22.0
                        else -> 34.0
                    }
                )

                // 2. Build favorite list models
                val items = favLocations.map { loc ->
                    val locTz = try {
                        if (!loc.timezone.isNullOrBlank()) java.time.ZoneId.of(loc.timezone) else java.time.ZoneId.systemDefault()
                    } catch (_: Exception) {
                        java.time.ZoneId.systemDefault()
                    }
                    val locTime = java.time.ZonedDateTime.now(locTz).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))

                    FavoriteItemUiModel(
                        location = loc,
                        temperature = when (loc.name) {
                            "Tokyo" -> 19.0
                            "Paris" -> 22.0
                            "New York" -> 16.0
                            "Sydney" -> 24.0
                            "Hà Nội" -> 28.0
                            "Thái Bình" -> 28.0
                            "Hưng Yên" -> 28.0
                            else -> 25.0
                        },
                        condition = when (loc.name) {
                            "Tokyo" -> WeatherCondition.CLEAR
                            "Paris" -> WeatherCondition.RAIN
                            "New York" -> WeatherCondition.CLOUDY
                            "Thái Bình", "Hưng Yên", "Hà Nội" -> WeatherCondition.CLEAR
                            else -> WeatherCondition.PARTLY_CLOUDY
                        },
                        localTime = locTime,
                        minTemp = 18.0,
                        maxTemp = 28.0
                    )
                }

                FavoritesUiState.Success(
                    currentLocation = currentActiveModel,
                    favoritesList = items
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun removeFavorite(locationId: String) {
        viewModelScope.launch {
            weatherRepository.removeFavoriteLocation(locationId)
        }
    }
}

/**
 * Stateful FavoritesScreen.
 */
@Composable
fun FavoritesScreen(
    onLocationSelected: (WeatherLocation) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    FavoritesContent(
        uiState = state,
        onLocationSelected = onLocationSelected,
        onRemoveFavorite = viewModel::removeFavorite,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless FavoritesContent strictly conforming to Stitch Screen `7b628323765946ca93f8222514e310d5`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
    uiState: FavoritesUiState,
    onLocationSelected: (WeatherLocation) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(strings.favoriteLocations, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.addLocation,
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            when (uiState) {
                is FavoritesUiState.Loading -> {
                    WeatherLoadingView(modifier = Modifier.align(Alignment.Center))
                }
                is FavoritesUiState.Success -> {
                    if (uiState.favoritesList.isEmpty() && uiState.currentLocation == null) {
                        WeatherEmptyView(
                            title = strings.noFavoritesTitle,
                            subtitle = strings.noFavoritesSubtitle,
                            icon = Icons.Default.Favorite,
                            actionText = strings.addLocation,
                            onAction = onNavigateToAdd,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                        ) {
                            // 1. Current Pinned Location Card (Stitch Component)
                            uiState.currentLocation?.let { current ->
                                item {
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onLocationSelected(current.location) },
                                        shape = RoundedCornerShape(20.dp),
                                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        contentPadding = 16.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = strings.pinnedCurrentLocation,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${current.location.name}, ${current.location.country ?: ""}",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                WeatherConditionIcon(condition = current.condition, size = 32.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${current.temperature.toInt()}°",
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Favorite Location Glass Cards (Stitch Component)
                            items(
                                items = uiState.favoritesList,
                                key = { it.location.id ?: it.location.name }
                            ) { item ->
                                FavoriteCityCard(
                                    item = item,
                                    onClick = { onLocationSelected(item.location) },
                                    onDelete = { onRemoveFavorite(item.location.id ?: "") }
                                )
                            }
                        }
                    }
                }
                is FavoritesUiState.Error -> {
                    WeatherEmptyView(
                        title = "Error loading favorites",
                        subtitle = uiState.message,
                        actionText = "Try Again",
                        onAction = { /* reload */ },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Stitch Favorite City Glass Card with local time, temperature, condition and delete action.
 */
@Composable
private fun FavoriteCityCard(
    item: FavoriteItemUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang = LocalAppLanguage.current
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.location.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.localTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val areaSubtitle = item.location.formattedArea.ifBlank { item.location.country ?: "" }
                if (areaSubtitle.isNotBlank()) {
                    Text(
                        text = areaSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${item.condition.localizedName(currentLang)} • H: ${item.maxTemp.toInt()}° L: ${item.minTemp.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WeatherConditionIcon(condition = item.condition, size = 36.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.temperature.toInt()}°",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Favorites Screen Dark", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun FavoritesScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        ProvideWeatherLanguage(language = AppLanguage.VIETNAMESE) {
            FavoritesContent(
                uiState = FavoritesUiState.Success(
                    currentLocation = FavoriteItemUiModel(
                        location = WeatherLocation(
                            id = "1",
                            name = "Hà Nội",
                            country = "Việt Nam",
                            latitude = 21.0,
                            longitude = 105.8
                        ),
                        temperature = 28.0,
                        condition = WeatherCondition.PARTLY_CLOUDY,
                        localTime = "09:00",
                        minTemp = 24.0,
                        maxTemp = 33.0
                    ),
                    favoritesList = listOf(
                        FavoriteItemUiModel(
                            location = WeatherLocation(
                                id = "2",
                                name = "Tokyo",
                                country = "Nhật Bản",
                                latitude = 35.6,
                                longitude = 139.6
                            ),
                            temperature = 19.0,
                            condition = WeatherCondition.CLEAR,
                            localTime = "16:30",
                            minTemp = 15.0,
                            maxTemp = 22.0
                        )
                    )
                ),
                onLocationSelected = {},
                onRemoveFavorite = {},
                onNavigateToAdd = {},
                onNavigateBack = {}
            )
        }
    }
}

@Preview(name = "Favorites Screen Light", showBackground = true)
@Composable
private fun FavoritesScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        ProvideWeatherLanguage(language = AppLanguage.ENGLISH) {
            FavoritesContent(
                uiState = FavoritesUiState.Success(
                    currentLocation = FavoriteItemUiModel(
                        location = WeatherLocation(
                            id = "1",
                            name = "Hanoi",
                            country = "Vietnam",
                            latitude = 21.0,
                            longitude = 105.8
                        ),
                        temperature = 28.0,
                        condition = WeatherCondition.PARTLY_CLOUDY,
                        localTime = "09:00",
                        minTemp = 24.0,
                        maxTemp = 33.0
                    )
                ),
                onLocationSelected = {},
                onRemoveFavorite = {},
                onNavigateToAdd = {},
                onNavigateBack = {}
            )
        }
    }
}
