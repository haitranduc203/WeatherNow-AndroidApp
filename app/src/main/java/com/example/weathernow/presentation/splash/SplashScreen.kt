package com.example.weathernow.presentation.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.presentation.util.LocalAppLanguage
import com.example.weathernow.theme.WeatherNowTheme
import com.example.weathernow.theme.WeatherPrimary
import com.example.weathernow.theme.WeatherSecondary
import com.example.weathernow.theme.WeatherTertiary
import com.example.weathernow.theme.atmosphericGradient
import kotlinx.coroutines.delay

/**
 * Animated Splash Screen for WeatherNow.
 * Displays glowing rotating weather emblems, branding typography, and transitions smoothly to the home destination.
 */
@Composable
fun SplashScreen(
    onFinishSplash: () -> Unit,
    modifier: Modifier = Modifier,
    splashDurationMs: Long = 1800L
) {
    val language = LocalAppLanguage.current
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.75f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteSunRotation")
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(splashDurationMs)
        onFinishSplash()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(atmosphericGradient()),
        contentAlignment = Alignment.Center
    ) {
        // 1. Ambient Background Glow Orbs
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(glowPulse)
                .alpha(0.18f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            WeatherTertiary,
                            WeatherPrimary,
                            Color.Transparent
                        )
                    )
                )
        )

        // 2. Main Branding Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Center: Animated Logo & Titles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            ) {
                // Weather Emblem with Layered Sun & Cloud
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating Sun Aura
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = WeatherTertiary.copy(alpha = 0.95f),
                        modifier = Modifier
                            .size(110.dp)
                            .rotate(sunRotation)
                    )

                    // Floating Glassmorphic Cloud Overlay
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(140.dp)
                            .scale(0.95f)
                            .alpha(0.35f)
                    ) {}

                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(82.dp)
                            .padding(top = 28.dp, start = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Name Title
                Text(
                    text = "WeatherNow",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline / Slogan
                Text(
                    text = if (language == AppLanguage.VIETNAMESE) {
                        "Thời tiết chính xác • Dự báo thông minh"
                    } else {
                        "Precision Weather • Smart Forecasts"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom: Loading Status & Version
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(alphaAnim)
                    .padding(bottom = 32.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = WeatherPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.VIETNAMESE) {
                                "Đang đồng bộ dữ liệu khí tượng..."
                            } else {
                                "Syncing atmospheric data..."
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "v1.0.0 • Clean Architecture",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview(name = "Splash Dark", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun SplashScreenDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        SplashScreen(onFinishSplash = {})
    }
}

@Preview(name = "Splash Light", showBackground = true)
@Composable
private fun SplashScreenLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        SplashScreen(onFinishSplash = {})
    }
}
