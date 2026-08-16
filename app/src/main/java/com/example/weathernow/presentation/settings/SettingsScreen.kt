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
import androidx.compose.material.icons.filled.Language
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
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.UserPreferences
import com.example.weathernow.domain.model.WindSpeedUnit
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.presentation.util.LocalWeatherStrings
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Shared in-memory preferences repository for instant reactivity across Compose trees.
 */
object UserPreferencesRepository {
    private val _preferencesFlow = MutableStateFlow(UserPreferences())
    val preferencesFlow: StateFlow<UserPreferences> = _preferencesFlow.asStateFlow()

    fun updateTheme(theme: AppTheme) {
        _preferencesFlow.value = _preferencesFlow.value.copy(theme = theme)
    }

    fun updateLanguage(language: AppLanguage) {
        _preferencesFlow.value = _preferencesFlow.value.copy(language = language)
    }

    fun updateTemperatureUnit(unit: TemperatureUnit) {
        _preferencesFlow.value = _preferencesFlow.value.copy(temperatureUnit = unit)
    }

    fun updateWindSpeedUnit(unit: WindSpeedUnit) {
        _preferencesFlow.value = _preferencesFlow.value.copy(windSpeedUnit = unit)
    }

    fun toggleDailySummary(enabled: Boolean) {
        _preferencesFlow.value = _preferencesFlow.value.copy(dailyNotificationEnabled = enabled)
    }

    fun toggleSevereAlerts(enabled: Boolean) {
        _preferencesFlow.value = _preferencesFlow.value.copy(severeWeatherAlertsEnabled = enabled)
    }

    fun toggleBackgroundSync(enabled: Boolean) {
        _preferencesFlow.value = _preferencesFlow.value.copy(backgroundRefreshEnabled = enabled)
    }
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val cachedDataSize: String = "4.8 MB",
    val cacheLastCleaned: String = "Updated 15m ago"
)

class SettingsViewModel : ViewModel() {
    private val _cacheState = MutableStateFlow("4.8 MB" to "Updated 15m ago")
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(UserPreferencesRepository.preferencesFlow, _cacheState) { prefs, cache ->
                SettingsUiState(
                    preferences = prefs,
                    cachedDataSize = cache.first,
                    cacheLastCleaned = cache.second
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setTheme(theme: AppTheme) = UserPreferencesRepository.updateTheme(theme)
    fun setLanguage(language: AppLanguage) = UserPreferencesRepository.updateLanguage(language)
    fun setTemperatureUnit(unit: TemperatureUnit) = UserPreferencesRepository.updateTemperatureUnit(unit)
    fun setWindSpeedUnit(unit: WindSpeedUnit) = UserPreferencesRepository.updateWindSpeedUnit(unit)
    fun toggleDailySummary(enabled: Boolean) = UserPreferencesRepository.toggleDailySummary(enabled)
    fun toggleSevereAlerts(enabled: Boolean) = UserPreferencesRepository.toggleSevereAlerts(enabled)
    fun toggleBackgroundSync(enabled: Boolean) = UserPreferencesRepository.toggleBackgroundSync(enabled)

    fun clearOfflineCache() {
        viewModelScope.launch {
            _cacheState.value = "0 KB" to "Cleared just now"
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
        onLanguageChanged = viewModel::setLanguage,
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
    onLanguageChanged: (AppLanguage) -> Unit,
    onTempUnitChanged: (TemperatureUnit) -> Unit,
    onWindUnitChanged: (WindSpeedUnit) -> Unit,
    onDailySummaryToggled: (Boolean) -> Unit,
    onSevereAlertsToggled: (Boolean) -> Unit,
    onBackgroundSyncToggled: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalWeatherStrings.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle, fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
        ) {
            // 1. Language Selection (Bilingual switcher)
            item {
                SettingsSectionHeader(title = strings.languageSection, icon = Icons.Default.Language)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 16.dp
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppLanguage.entries.forEachIndexed { index, lang ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppLanguage.entries.size),
                                onClick = { onLanguageChanged(lang) },
                                selected = lang == uiState.preferences.language,
                                label = { Text(lang.displayName, fontWeight = FontWeight.Medium) }
                            )
                        }
                    }
                }
            }

            // 2. Units & Appearance Section (Stitch Component)
            item {
                SettingsSectionHeader(title = strings.unitsAppearanceSection, icon = Icons.Default.Palette)
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
                                text = strings.themeMode,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                AppTheme.entries.forEachIndexed { index, theme ->
                                    val label = when (theme) {
                                        AppTheme.SYSTEM -> strings.themeSystem
                                        AppTheme.LIGHT -> strings.themeLight
                                        AppTheme.DARK -> strings.themeDark
                                    }
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTheme.entries.size),
                                        onClick = { onThemeChanged(theme) },
                                        selected = theme == uiState.preferences.theme,
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }

                        // Temperature Unit Selector
                        Column {
                            Text(
                                text = strings.temperatureUnit,
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
                                text = strings.windUnit,
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

            // 3. Updates & Notifications Section (Stitch Component)
            item {
                SettingsSectionHeader(title = strings.updatesNotificationsSection, icon = Icons.Default.Notifications)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsToggleRow(
                            title = strings.dailySummary,
                            description = strings.dailySummaryDesc,
                            checked = uiState.preferences.dailyNotificationEnabled,
                            onCheckedChange = onDailySummaryToggled
                        )
                        SettingsToggleRow(
                            title = strings.severeAlerts,
                            description = strings.severeAlertsDesc,
                            checked = uiState.preferences.severeWeatherAlertsEnabled,
                            onCheckedChange = onSevereAlertsToggled
                        )
                        SettingsToggleRow(
                            title = strings.backgroundRefresh,
                            description = strings.backgroundRefreshDesc,
                            checked = uiState.preferences.backgroundRefreshEnabled,
                            onCheckedChange = onBackgroundSyncToggled
                        )
                    }
                }
            }

            // 4. Data & Offline Storage Section (Stitch Component)
            item {
                SettingsSectionHeader(title = strings.offlineStorageSection, icon = Icons.Default.Storage)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.offlineCache,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.cachedDataSize} • ${uiState.cacheLastCleaned}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onClearCache,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(strings.clearCache)
                        }
                    }
                }
            }

            // 5. About & Info Section (Stitch Component)
            item {
                SettingsSectionHeader(title = strings.aboutSection, icon = Icons.Default.Info)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.weatherProvider,
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
                                text = strings.appVersion,
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
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
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
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Preview(name = "Settings Screen Dark", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun SettingsScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        ProvideWeatherLanguage(language = AppLanguage.VIETNAMESE) {
            SettingsContent(
                uiState = SettingsUiState(),
                onThemeChanged = {},
                onLanguageChanged = {},
                onTempUnitChanged = {},
                onWindUnitChanged = {},
                onDailySummaryToggled = {},
                onSevereAlertsToggled = {},
                onBackgroundSyncToggled = {},
                onClearCache = {},
                onNavigateBack = {}
            )
        }
    }
}

@Preview(name = "Settings Screen Light", showBackground = true)
@Composable
private fun SettingsScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        ProvideWeatherLanguage(language = AppLanguage.ENGLISH) {
            SettingsContent(
                uiState = SettingsUiState(),
                onThemeChanged = {},
                onLanguageChanged = {},
                onTempUnitChanged = {},
                onWindUnitChanged = {},
                onDailySummaryToggled = {},
                onSevereAlertsToggled = {},
                onBackgroundSyncToggled = {},
                onClearCache = {},
                onNavigateBack = {}
            )
        }
    }
}
