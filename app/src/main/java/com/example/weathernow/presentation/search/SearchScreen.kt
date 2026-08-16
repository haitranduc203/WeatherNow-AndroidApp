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
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.components.WeatherEmptyView
import com.example.weathernow.presentation.components.WeatherErrorView
import com.example.weathernow.presentation.components.WeatherLoadingView
import com.example.weathernow.presentation.util.LocalWeatherStrings
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.WeatherNowTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiModel(
    val query: String = "",
    val searchResults: List<WeatherLocation> = emptyList(),
    val recentSearches: List<WeatherLocation> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

class SearchViewModel(
    private val locationRepository: com.example.weathernow.domain.repository.LocationRepository = com.example.weathernow.data.repository.LocationRepositoryImpl(),
    private val weatherRepository: com.example.weathernow.domain.repository.WeatherRepository = com.example.weathernow.data.repository.WeatherRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SearchUiModel(
            recentSearches = listOf(
                WeatherLocation(id = "1581130", name = "Hanoi", country = "Vietnam", adminArea = "Ha Noi", latitude = 21.0285, longitude = 105.8542),
                WeatherLocation(id = "1850147", name = "Tokyo", country = "Japan", adminArea = "Tokyo", latitude = 35.6762, longitude = 139.6503),
                WeatherLocation(id = "2988507", name = "Paris", country = "France", adminArea = "Île-de-France", latitude = 48.8566, longitude = 2.3522)
            )
        )
    )
    val uiState: StateFlow<SearchUiModel> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false,
                errorMessage = null
            )
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, errorMessage = null)
            delay(400)
            when (val result = locationRepository.searchLocations(newQuery)) {
                is com.example.weathernow.core.common.Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        searchResults = result.data,
                        isSearching = false,
                        errorMessage = null
                    )
                }
                is com.example.weathernow.core.common.Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        errorMessage = result.message
                    )
                }
                is com.example.weathernow.core.common.Resource.Loading -> {}
            }
        }
    }

    fun clearQuery() {
        onQueryChange("")
    }

    fun toggleFavorite(location: WeatherLocation) {
        viewModelScope.launch {
            if (location.isFavorite) {
                weatherRepository.removeFavoriteLocation(location.id ?: location.name)
            } else {
                weatherRepository.addFavoriteLocation(location)
            }
            val updated = _uiState.value.searchResults.map {
                if (it.id == location.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            _uiState.value = _uiState.value.copy(searchResults = updated)
        }
    }

    fun removeRecentSearch(location: WeatherLocation) {
        val updated = _uiState.value.recentSearches.filterNot { it.id == location.id }
        _uiState.value = _uiState.value.copy(recentSearches = updated)
    }

    fun clearAllRecentSearches() {
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }
}

/**
 * Stateful SearchScreen.
 */
@Composable
fun SearchScreen(
    onLocationSelected: (WeatherLocation) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    SearchContent(
        content = state,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::clearQuery,
        onLocationSelected = onLocationSelected,
        onToggleFavorite = viewModel::toggleFavorite,
        onRemoveRecent = viewModel::removeRecentSearch,
        onClearAllRecent = viewModel::clearAllRecentSearches,
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
    content: SearchUiModel,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    onToggleFavorite: (WeatherLocation) -> Unit,
    onRemoveRecent: (WeatherLocation) -> Unit,
    onClearAllRecent: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(strings.navSearch, fontWeight = FontWeight.Bold) },
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
                        strings.searchPlaceholder,
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
                        onClick = { onQueryChange("Hanoi") },
                        label = { Text(strings.useMyLocation, color = MaterialTheme.colorScheme.primary) },
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
                            text = strings.recentSearches,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onClearAllRecent) {
                            Text(strings.clearAll, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
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
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    WeatherEmptyView(
                        title = strings.noRecentSearches,
                        subtitle = strings.searchPlaceholder,
                        icon = Icons.Default.Search,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Search Results List (Stitch Component)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
                ) {
                    items(content.searchResults) { result ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(result) },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = 14.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = result.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = result.country ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${String.format("%.2f", result.latitude)}°, ${String.format("%.2f", result.longitude)}°",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { onToggleFavorite(result) }) {
                                    Icon(
                                        imageVector = if (result.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (result.isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Search Screen Dark", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun SearchScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        ProvideWeatherLanguage(language = AppLanguage.VIETNAMESE) {
            SearchContent(
                content = SearchUiModel(
                    recentSearches = listOf(
                        WeatherLocation(id = "1", name = "Hà Nội", country = "Việt Nam", latitude = 21.0, longitude = 105.8),
                        WeatherLocation(id = "2", name = "Tokyo", country = "Nhật Bản", latitude = 35.6, longitude = 139.6)
                    )
                ),
                onQueryChange = {},
                onClearQuery = {},
                onLocationSelected = {},
                onToggleFavorite = {},
                onRemoveRecent = {},
                onClearAllRecent = {},
                onNavigateBack = {}
            )
        }
    }
}

@Preview(name = "Search Screen Light", showBackground = true)
@Composable
private fun SearchScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        ProvideWeatherLanguage(language = AppLanguage.ENGLISH) {
            SearchContent(
                content = SearchUiModel(
                    query = "London",
                    searchResults = listOf(
                        WeatherLocation(id = "1", name = "London", country = "United Kingdom", latitude = 51.5, longitude = -0.1)
                    )
                ),
                onQueryChange = {},
                onClearQuery = {},
                onLocationSelected = {},
                onToggleFavorite = {},
                onRemoveRecent = {},
                onClearAllRecent = {},
                onNavigateBack = {}
            )
        }
    }
}
