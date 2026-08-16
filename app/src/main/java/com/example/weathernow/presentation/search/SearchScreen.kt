package com.example.weathernow.presentation.search

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.components.WeatherEmptyView
import com.example.weathernow.presentation.components.WeatherErrorView
import com.example.weathernow.presentation.components.WeatherLoadingView
import com.example.weathernow.theme.AtmosphericGradientDark
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data class Content(
        val query: String = "",
        val isSearching: Boolean = false,
        val recentSearches: List<WeatherLocation> = emptyList(),
        val searchResults: List<WeatherLocation> = emptyList(),
        val errorMessage: String? = null
    ) : SearchUiState
}

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState.Content(
        recentSearches = listOf(
            WeatherLocation(id = "1", name = "Hanoi", country = "Vietnam", latitude = 21.0285, longitude = 105.8542, isFavorite = true),
            WeatherLocation(id = "2", name = "Tokyo", country = "Japan", latitude = 35.6762, longitude = 139.6503, isFavorite = false),
            WeatherLocation(id = "3", name = "Paris", country = "France", latitude = 48.8566, longitude = 2.3522, isFavorite = true)
        ),
        searchResults = listOf(
            WeatherLocation(id = "1", name = "Hanoi", country = "Vietnam", latitude = 21.0285, longitude = 105.8542, isFavorite = true),
            WeatherLocation(id = "4", name = "Haiphong", country = "Vietnam", latitude = 20.8449, longitude = 106.6881, isFavorite = false),
            WeatherLocation(id = "5", name = "Ha Tinh", country = "Vietnam", latitude = 18.3429, longitude = 105.9059, isFavorite = false)
        )
    ))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(newQuery: String) {
        val current = _uiState.value
        _uiState.value = current.copy(query = newQuery)
    }

    fun clearQuery() {
        val current = _uiState.value
        _uiState.value = current.copy(query = "")
    }

    fun clearAllRecent() {
        val current = _uiState.value
        _uiState.value = current.copy(recentSearches = emptyList())
    }

    fun removeRecent(location: WeatherLocation) {
        val current = _uiState.value
        val updated = current.recentSearches.filter { it.id != location.id }
        _uiState.value = current.copy(recentSearches = updated)
    }

    fun toggleFavorite(location: WeatherLocation) {
        viewModelScope.launch {
            val current = _uiState.value
            val updatedResults = current.searchResults.map {
                if (it.id == location.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            _uiState.value = current.copy(searchResults = updatedResults)
        }
    }
}

/**
 * Stateful SearchScreen.
 */
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    SearchContent(
        uiState = state,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::clearQuery,
        onClearAllRecent = viewModel::clearAllRecent,
        onRemoveRecent = viewModel::removeRecent,
        onToggleFavorite = viewModel::toggleFavorite,
        onLocationSelected = onLocationSelected,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless SearchContent strictly conforming to Stitch Screen `7ce90a40b89e48938732dab29a208268`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onClearAllRecent: () -> Unit,
    onRemoveRecent: (WeatherLocation) -> Unit,
    onToggleFavorite: (WeatherLocation) -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val content = when (uiState) {
        is SearchUiState.Content -> uiState
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Search Location", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.weathernow.theme.atmosphericGradient())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Search Bar (Stitch Component)
            OutlinedTextField(
                value = content.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = {
                    Text(
                        "Search city or country (e.g. Hanoi, Tokyo)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (content.query.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            )

            // 2. Quick Action Chips (Stitch Component)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { onQueryChange("Current Location") },
                        label = { Text("Use My Location", color = MaterialTheme.colorScheme.primary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = CircleShape,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            borderWidth = 1.dp
                        )
                    )
                }
                val popularCities = listOf("Tokyo", "Paris", "New York", "Singapore", "London", "Sydney")
                items(popularCities) { city ->
                    AssistChip(
                        onClick = { onQueryChange(city) },
                        label = { Text(city, color = MaterialTheme.colorScheme.onSurface) },
                        shape = CircleShape,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Main Body: Results or Recent Searches
            if (content.isSearching) {
                WeatherLoadingView(message = "Searching locations...")
            } else if (content.errorMessage != null) {
                WeatherErrorView(
                    errorMessage = content.errorMessage,
                    onRetry = { /* Retry query */ }
                )
            } else if (content.query.isEmpty()) {
                // Recent Searches Section (Stitch Component)
                if (content.recentSearches.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onClearAllRecent) {
                            Text("Clear all", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(content.recentSearches) { recent ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLocationSelected(recent) },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = recent.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = recent.country ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onRemoveRecent(recent) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    WeatherEmptyView(
                        title = "Explore World Weather",
                        subtitle = "Search for a city or country to view live forecasts and atmospheric data.",
                        icon = Icons.Default.Search
                    )
                }
            } else {
                // Search Results List (Stitch Component)
                Text(
                    text = "Search Results",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (content.searchResults.isEmpty()) {
                    WeatherEmptyView(
                        title = "No Locations Found",
                        subtitle = "Try searching with a different city or country name.",
                        icon = Icons.Default.Search
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(content.searchResults) { location ->
                            SearchResultCard(
                                location = location,
                                onCardClick = { onLocationSelected(location) },
                                onToggleFavorite = { onToggleFavorite(location) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    location: WeatherLocation,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
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
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    location.country?.let { country ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = WeatherPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = country,
                                style = MaterialTheme.typography.labelSmall,
                                color = WeatherPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lat: ${"%.2f".format(location.latitude)}, Lon: ${"%.2f".format(location.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (location.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (location.isFavorite) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "SearchScreen Dark Mode", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun SearchScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        SearchScreen(
            onNavigateBack = {},
            onLocationSelected = {}
        )
    }
}

@Preview(name = "SearchScreen Light Mode", showBackground = true)
@Composable
private fun SearchScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        SearchScreen(
            onNavigateBack = {},
            onLocationSelected = {}
        )
    }
}
