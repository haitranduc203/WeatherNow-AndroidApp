package com.example.weathernow.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val isLoading: Boolean = false,
    val locationName: String = "Select Location",
    val temperature: String? = null,
    val condition: String? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

@OptIn(ExperimentalMaterial3Api::class)
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
        state = state,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToForecast = onNavigateToForecast,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
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
                title = { Text("WeatherNow") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search Location")
                    }
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorite Locations")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = state.locationName,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Welcome to WeatherNow",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onNavigateToSearch) {
                        Text("Search City / Location")
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Light Mode", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = false) {
        HomeContent(
            state = HomeUiState(locationName = "Hanoi, Vietnam"),
            onNavigateToSearch = {},
            onNavigateToFavorites = {},
            onNavigateToSettings = {},
            onNavigateToForecast = { _, _, _ -> }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun HomeScreenDarkPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = true) {
        HomeContent(
            state = HomeUiState(locationName = "Hanoi, Vietnam"),
            onNavigateToSearch = {},
            onNavigateToFavorites = {},
            onNavigateToSettings = {},
            onNavigateToForecast = { _, _, _ -> }
        )
    }
}
