package com.kikepb.onboarding.presentation.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kikepb.onboarding.presentation.model.OnboardingPageDataUiModel
import com.kikepb.onboarding.presentation.model.OnboardingPageDataUiModel.Ready
import com.kikepb.onboarding.presentation.model.OnboardingPageDataUiModel.Stats
import com.kikepb.onboarding.presentation.model.OnboardingPageDataUiModel.Welcome

@Composable
fun OnboardingPageDataUiModel.gradientStart(): Color = when (this) {
    Welcome -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
    Stats -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
    Ready -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
}

@Composable
fun OnboardingPageDataUiModel.gradientEnd(): Color = MaterialTheme.colorScheme.background

@Composable
fun OnboardingPageDataUiModel.illustrationColor(): Color = when (this) {
    Welcome -> MaterialTheme.colorScheme.primary
    Stats -> MaterialTheme.colorScheme.secondary
    Ready -> MaterialTheme.colorScheme.tertiary
}