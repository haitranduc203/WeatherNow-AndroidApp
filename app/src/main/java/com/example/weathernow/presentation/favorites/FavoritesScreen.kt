package com.example.weathernow.presentation.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.example.weathernow.domain.model.WeatherLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FavoritesUiState(
    val favorites: List<WeatherLocation> = emptyList(),
    val isLoading: Boolean = false
)

class FavoritesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    FavoritesContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onLocationSelected = onLocationSelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
    state: FavoritesUiState,
    onNavigateBack: () -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Favorite Locations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (state.favorites.isEmpty()) {
                Text("No favorite locations added yet.")
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Light Mode", showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = false) {
        FavoritesContent(
            state = FavoritesUiState(
                favorites = listOf(
                    WeatherLocation(id = "1", name = "Paris", country = "France", latitude = 48.85, longitude = 2.35, timezone = "Europe/Paris")
                )
            ),
            onNavigateBack = {},
            onLocationSelected = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun FavoritesScreenDarkPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = true) {
        FavoritesContent(
            state = FavoritesUiState(
                favorites = listOf(
                    WeatherLocation(id = "1", name = "Paris", country = "France", latitude = 48.85, longitude = 2.35, timezone = "Europe/Paris")
                )
            ),
            onNavigateBack = {},
            onLocationSelected = {}
        )
    }
}
