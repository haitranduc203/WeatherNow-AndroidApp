package com.example.weathernow.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel()

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Welcome to WeatherNow",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Accurate forecasts, offline caching, and real-time updates.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onFinishOnboarding) {
                Text("Get Started")
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Light Mode", showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = false) {
        OnboardingScreen(onFinishOnboarding = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun OnboardingScreenDarkPreview() {
    com.example.weathernow.theme.WeatherNowTheme(darkTheme = true) {
        OnboardingScreen(onFinishOnboarding = {})
    }
}
