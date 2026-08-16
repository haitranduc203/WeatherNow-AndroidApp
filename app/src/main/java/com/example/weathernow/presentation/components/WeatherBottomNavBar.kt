package com.example.weathernow.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weathernow.presentation.navigation.WeatherNavDestination
import com.example.weathernow.presentation.util.LocalWeatherStrings
import com.example.weathernow.theme.GlassCardBorderDark
import com.example.weathernow.theme.GlassCardBorderLight
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherPrimaryContainer
import com.example.weathernow.theme.WeatherPrimaryLight

enum class NavigationTab(
    val icon: ImageVector,
    val destination: WeatherNavDestination
) {
    HOME(Icons.Default.Home, WeatherNavDestination.Home),
    SEARCH(Icons.Default.Search, WeatherNavDestination.Search),
    FAVORITES(Icons.Default.Favorite, WeatherNavDestination.Favorites),
    SETTINGS(Icons.Default.Settings, WeatherNavDestination.Settings);

    @Composable
    fun getLabel(): String {
        val strings = LocalWeatherStrings.current
        return when (this) {
            HOME -> strings.navHome
            SEARCH -> strings.navSearch
            FAVORITES -> strings.navFavorites
            SETTINGS -> strings.navSettings
        }
    }
}

/**
 * Stitch-inspired floating Glassmorphic Bottom Navigation Bar.
 */
@Composable
fun WeatherBottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF10141A).copy(alpha = 0.88f) else Color(0xFFFFFFFF).copy(alpha = 0.92f)
    val borderColor = if (isDark) GlassCardBorderDark else GlassCardBorderLight

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp)),
        color = bgColor,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(64.dp)
        ) {
            NavigationTab.entries.forEach { tab ->
                val selected = tab == currentTab
                val labelText = tab.getLabel()
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = labelText
                        )
                    },
                    label = {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) WeatherPrimary else WeatherPrimaryLight,
                        selectedTextColor = if (isDark) WeatherPrimary else WeatherPrimaryLight,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        indicatorColor = if (isDark) WeatherPrimaryContainer.copy(alpha = 0.45f) else Color(0xFFE0E3F9)
                    )
                )
            }
        }
    }
}

@Preview(name = "Bottom Nav Bar Dark", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun WeatherBottomNavBarDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        WeatherBottomNavBar(
            currentTab = NavigationTab.HOME,
            onTabSelected = {}
        )
    }
}

@Preview(name = "Bottom Nav Bar Light", showBackground = true)
@Composable
private fun WeatherBottomNavBarLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        WeatherBottomNavBar(
            currentTab = NavigationTab.SEARCH,
            onTabSelected = {}
        )
    }
}
