package com.example.weathernow.presentation.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.UserPreferences
import com.example.weathernow.domain.model.WindSpeedUnit
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.theme.AtmosphericGradientDark
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val cachedDataSize: String = "4.8 MB",
    val cacheLastCleaned: String = "Updated 15m ago"
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(theme = theme)
        )
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(temperatureUnit = unit)
        )
    }

    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(windSpeedUnit = unit)
        )
    }

    fun toggleDailySummary(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(dailyNotificationEnabled = enabled)
        )
    }

    fun toggleSevereAlerts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(severeWeatherAlertsEnabled = enabled)
        )
    }

    fun toggleBackgroundSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(backgroundRefreshEnabled = enabled)
        )
    }

    fun clearOfflineCache() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cachedDataSize = "0 KB",
                cacheLastCleaned = "Cleared just now"
            )
        }
    }
}

/**
 * Stateful SettingsScreen.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = state,
        onThemeChanged = viewModel::setTheme,
        onTempUnitChanged = viewModel::setTemperatureUnit,
        onWindUnitChanged = viewModel::setWindSpeedUnit,
        onDailySummaryToggled = viewModel::toggleDailySummary,
        onSevereAlertsToggled = viewModel::toggleSevereAlerts,
        onBackgroundSyncToggled = viewModel::toggleBackgroundSync,
        onClearCache = viewModel::clearOfflineCache,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless SettingsContent strictly conforming to Stitch Screen `712f8a8a1a6843d792bf265c499abc12`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onThemeChanged: (AppTheme) -> Unit,
    onTempUnitChanged: (TemperatureUnit) -> Unit,
    onWindUnitChanged: (WindSpeedUnit) -> Unit,
    onDailySummaryToggled: (Boolean) -> Unit,
    onSevereAlertsToggled: (Boolean) -> Unit,
    onBackgroundSyncToggled: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.weathernow.theme.atmosphericGradient())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            // 1. Units & Appearance Section (Stitch Component)
            item {
                SettingsSectionHeader(title = "Units & Appearance", icon = Icons.Default.Palette)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Theme Selector
                        Column {
                            Text(
                                text = "Theme Mode",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                AppTheme.entries.forEachIndexed { index, theme ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTheme.entries.size),
                                        onClick = { onThemeChanged(theme) },
                                        selected = theme == uiState.preferences.theme,
                                        label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }
                        }

                        // Temperature Unit Selector
                        Column {
                            Text(
                                text = "Temperature Unit",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                TemperatureUnit.entries.forEachIndexed { index, unit ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TemperatureUnit.entries.size),
                                        onClick = { onTempUnitChanged(unit) },
                                        selected = unit == uiState.preferences.temperatureUnit,
                                        label = { Text(if (unit == TemperatureUnit.CELSIUS) "°C Celsius" else "°F Fahrenheit") }
                                    )
                                }
                            }
                        }

                        // Wind Speed Unit Selector
                        Column {
                            Text(
                                text = "Wind Speed Unit",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                WindSpeedUnit.entries.forEachIndexed { index, unit ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = WindSpeedUnit.entries.size),
                                        onClick = { onWindUnitChanged(unit) },
                                        selected = unit == uiState.preferences.windSpeedUnit,
                                        label = { Text(unit.name.lowercase()) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Background Updates & Alerts Section (Stitch Component)
            item {
                SettingsSectionHeader(title = "Updates & Notifications", icon = Icons.Default.Notifications)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SettingToggleRow(
                            title = "Daily Weather Summary",
                            subtitle = "Receive a morning briefing at 07:00 AM",
                            checked = uiState.preferences.dailyNotificationEnabled,
                            onCheckedChange = onDailySummaryToggled
                        )
                        SettingToggleRow(
                            title = "Severe Weather Alerts",
                            subtitle = "Immediate push alerts for storms and high UV",
                            checked = uiState.preferences.severeWeatherAlertsEnabled,
                            onCheckedChange = onSevereAlertsToggled
                        )
                        SettingToggleRow(
                            title = "Background Weather Refresh",
                            subtitle = "Periodically sync weather in background",
                            badgeText = "Every 3 hours",
                            checked = uiState.preferences.backgroundRefreshEnabled,
                            onCheckedChange = onBackgroundSyncToggled
                        )
                    }
                }
            }

            // 3. Offline Cache & Storage (Stitch Component)
            item {
                SettingsSectionHeader(title = "Data & Offline Storage", icon = Icons.Default.Storage)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Offline Weather Cache",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.cachedDataSize} cached • ${uiState.cacheLastCleaned}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onClearCache,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear Cache", color = WeatherPrimary)
                        }
                    }
                }
            }

            // 4. About Section (Stitch Component)
            item {
                SettingsSectionHeader(title = "About WeatherNow", icon = Icons.Default.Info)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Weather Data Provider",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Open-Meteo API",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Application Version",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "v1.0.0 (Native Android)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WeatherPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badgeText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (badgeText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = WeatherPrimary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = WeatherPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WeatherPrimary,
                checkedTrackColor = WeatherPrimary.copy(alpha = 0.35f)
            )
        )
    }
}

@Preview(name = "SettingsScreen Dark Mode", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun SettingsScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        SettingsScreen(
            onNavigateBack = {}
        )
    }
}

@Preview(name = "SettingsScreen Light Mode", showBackground = true)
@Composable
private fun SettingsScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        SettingsScreen(
            onNavigateBack = {}
        )
    }
}
