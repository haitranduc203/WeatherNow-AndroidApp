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
import androidx.compose.material.icons.filled.DeleteOutline
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
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.components.WeatherConditionIcon
import com.example.weathernow.presentation.components.WeatherEmptyView
import com.example.weathernow.presentation.components.WeatherLoadingView
import com.example.weathernow.theme.AtmosphericGradientDark
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoriteCityWeather(
    val location: WeatherLocation,
    val temperature: Double,
    val condition: WeatherCondition,
    val minTemp: Double,
    val maxTemp: Double,
    val localTime: String
)

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Success(
        val currentLocation: FavoriteCityWeather? = null,
        val favoritesList: List<FavoriteCityWeather> = emptyList()
    ) : FavoritesUiState
}

class FavoritesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Success(
        currentLocation = FavoriteCityWeather(
            location = WeatherLocation(id = "1", name = "Hanoi", country = "Vietnam", latitude = 21.0285, longitude = 105.8542, isFavorite = true),
            temperature = 28.0,
            condition = WeatherCondition.PARTLY_CLOUDY,
            minTemp = 24.0,
            maxTemp = 33.0,
            localTime = "14:30"
        ),
        favoritesList = listOf(
            FavoriteCityWeather(
                location = WeatherLocation(id = "2", name = "Tokyo", country = "Japan", latitude = 35.6762, longitude = 139.6503, isFavorite = true),
                temperature = 19.0,
                condition = WeatherCondition.CLEAR,
                minTemp = 15.0,
                maxTemp = 22.0,
                localTime = "16:30"
            ),
            FavoriteCityWeather(
                location = WeatherLocation(id = "3", name = "Paris", country = "France", latitude = 48.8566, longitude = 2.3522, isFavorite = true),
                temperature = 22.0,
                condition = WeatherCondition.RAIN,
                minTemp = 17.0,
                maxTemp = 24.0,
                localTime = "08:30"
            ),
            FavoriteCityWeather(
                location = WeatherLocation(id = "4", name = "New York", country = "USA", latitude = 40.7128, longitude = -74.0060, isFavorite = true),
                temperature = 16.0,
                condition = WeatherCondition.PARTLY_CLOUDY,
                minTemp = 12.0,
                maxTemp = 20.0,
                localTime = "02:30"
            ),
            FavoriteCityWeather(
                location = WeatherLocation(id = "5", name = "Sydney", country = "Australia", latitude = -33.8688, longitude = 151.2093, isFavorite = true),
                temperature = 25.0,
                condition = WeatherCondition.CLEAR,
                minTemp = 19.0,
                maxTemp = 28.0,
                localTime = "18:30"
            )
        )
    ))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun removeFavorite(item: FavoriteCityWeather) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is FavoritesUiState.Success) {
                val updated = current.favoritesList.filter { it.location.id != item.location.id }
                _uiState.value = current.copy(favoritesList = updated)
            }
        }
    }
}

/**
 * Stateful FavoritesScreen.
 */
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    onNavigateToAdd: () -> Unit = onNavigateBack,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    FavoritesContent(
        uiState = state,
        onRemoveFavorite = viewModel::removeFavorite,
        onLocationSelected = onLocationSelected,
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
    onRemoveFavorite: (FavoriteCityWeather) -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Favorite Locations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Location") },
                containerColor = WeatherPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 64.dp)
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
            when (uiState) {
                is FavoritesUiState.Loading -> {
                    WeatherLoadingView(modifier = Modifier.align(Alignment.Center))
                }
                is FavoritesUiState.Success -> {
                    if (uiState.favoritesList.isEmpty() && uiState.currentLocation == null) {
                        WeatherEmptyView(
                            title = "No Favorite Cities Added",
                            subtitle = "Keep track of weather in your favorite destinations by tapping Add Location.",
                            icon = Icons.Default.Favorite,
                            actionText = "Add Location",
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
                                        backgroundColor = WeatherPrimary.copy(alpha = 0.15f),
                                        borderColor = WeatherPrimary.copy(alpha = 0.35f),
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
                                                    tint = WeatherPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "Current Location",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = WeatherPrimary
                                                    )
                                                    Text(
                                                        text = "${current.location.name}${if (!current.location.country.isNullOrEmpty()) ", ${current.location.country}" else ""}",
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

                            // 2. Saved Favorites List (Stitch Component)
                            items(uiState.favoritesList) { item ->
                                FavoriteLocationCard(
                                    item = item,
                                    onClick = { onLocationSelected(item.location) },
                                    onDelete = { onRemoveFavorite(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteLocationCard(
    item: FavoriteCityWeather,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 18.dp
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
                        color = Color.White.copy(alpha = 0.08f),
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
                Text(
                    text = item.location.country ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${item.condition.displayName} • H: ${item.maxTemp.toInt()}° L: ${item.minTemp.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = WeatherSecondary
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
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "FavoritesScreen Dark Mode", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun FavoritesScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        FavoritesScreen(
            onNavigateBack = {},
            onLocationSelected = {},
            onNavigateToAdd = {}
        )
    }
}

@Preview(name = "FavoritesScreen Light Mode", showBackground = true)
@Composable
private fun FavoritesScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        FavoritesScreen(
            onNavigateBack = {},
            onLocationSelected = {},
            onNavigateToAdd = {}
        )
    }
}
