package com.example.weathernow.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.presentation.components.GlassCard
import com.example.weathernow.theme.AtmosphericGradientDark
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherSecondary
import com.example.weathernow.theme.WeatherTertiary

/**
 * Stateful OnboardingScreen.
 */
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingContent(
        onEnableLocation = onFinishOnboarding,
        onSetManualLocation = onFinishOnboarding,
        onSkip = onFinishOnboarding,
        modifier = modifier
    )
}

/**
 * Stateless OnboardingContent strictly conforming to Stitch Screen `06756165ec5142a9aa4335a1421f5fb9`.
 */
@Composable
fun OnboardingContent(
    onEnableLocation: () -> Unit,
    onSetManualLocation: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.weathernow.theme.atmosphericGradient())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar with Logo Pill & Skip
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = WeatherPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = WeatherTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WeatherNow",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = WeatherPrimary
                        )
                    }
                }

                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Hero Atmospheric Visual
            GlassCard(
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                contentPadding = 0.dp
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = WeatherTertiary,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Headline & Description
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Precision Weather\nAt Your Fingertips",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Hyper-local forecasts, hourly precipitation radar, and seamless offline caching wherever you go.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Feature Highlights (Stitch Component)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OnboardingFeatureRow(
                    icon = Icons.Default.MyLocation,
                    iconTint = WeatherSecondary,
                    title = "Hyper-Local Precision",
                    description = "Accurate forecasts down to your immediate neighborhood"
                )
                OnboardingFeatureRow(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    iconTint = WeatherPrimary,
                    title = "24-Hour & 7-Day Trends",
                    description = "Interactive graphs, precipitation shifts & solar arcs"
                )
                OnboardingFeatureRow(
                    icon = Icons.Default.CloudDone,
                    iconTint = WeatherTertiary,
                    title = "100% Offline Ready",
                    description = "Instant access to cached forecasts without internet"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 5. Glowing Page Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(WeatherPrimary)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEnableLocation,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WeatherPrimary)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Location & Start",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                TextButton(onClick = onSetManualLocation) {
                    Text(
                        text = "Set Location Manually",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = iconTint.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "OnboardingScreen Dark Mode", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun OnboardingScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        OnboardingScreen(onFinishOnboarding = {})
    }
}

@Preview(name = "OnboardingScreen Light Mode", showBackground = true)
@Composable
private fun OnboardingScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        OnboardingScreen(onFinishOnboarding = {})
    }
}
