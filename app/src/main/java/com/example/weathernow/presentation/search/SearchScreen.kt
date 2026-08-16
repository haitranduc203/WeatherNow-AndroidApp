package com.example.weathernow.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.weathernow.domain.model.WeatherLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<WeatherLocation> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    SearchContent(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onNavigateBack = onNavigateBack,
        onLocationSelected = onLocationSelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onLocationSelected: (WeatherLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Search Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter city name (e.g. Hanoi, Tokyo)...") },
                singleLine = true
            )
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (state.query.isBlank()) {
                    Text("Type a city name to search")
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Light Mode", showBackground = true)
@Composable
private fun SearchScreenPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = false) {
        SearchContent(
            state = SearchUiState(query = "Tokyo"),
            onQueryChanged = {},
            onNavigateBack = {},
            onLocationSelected = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun SearchScreenDarkPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = true) {
        SearchContent(
            state = SearchUiState(query = "Tokyo"),
            onQueryChanged = {},
            onNavigateBack = {},
            onLocationSelected = {}
        )
    }
}
