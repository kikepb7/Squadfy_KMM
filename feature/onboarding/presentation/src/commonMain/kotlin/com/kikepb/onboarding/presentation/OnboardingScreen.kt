package com.kikepb.onboarding.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.onboarding.presentation.OnboardingEvent.OnFinished
import com.kikepb.onboarding.presentation.model.OnboardingPageDataUiModel
import com.kikepb.onboarding.presentation.utils.gradientEnd
import com.kikepb.onboarding.presentation.utils.gradientStart
import com.kikepb.onboarding.presentation.utils.illustrationColor
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue

@Composable
fun OnboardingRoot(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinished: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            OnFinished -> onFinished()
        }
    }

    OnboardingScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun OnboardingScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { state.pageCount })
    val currentPage = state.pages.getOrNull(state.currentPage) ?: return

    LaunchedEffect(key1 = state.currentPage) {
        if (pagerState.currentPage != state.currentPage) pagerState.animateScrollToPage(state.currentPage)
    }

    LaunchedEffect(key1 = pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onAction(OnboardingAction.OnPageChanged(page))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        val backgroundStart by animateColorAsState(
            targetValue = currentPage.gradientStart(),
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            label = "backgroundStart"
        )
        val backgroundEnd by animateColorAsState(
            targetValue = currentPage.gradientEnd(),
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            label = "backgroundEnd"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Brush.verticalGradient(colors = listOf(backgroundStart, backgroundEnd)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val skipAlpha by animateFloatAsState(
                    targetValue = if (state.isLastPage) 0f else 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "skipAlpha"
                )
                TextButton(
                    onClick = { onAction(OnboardingAction.OnSkip) },
                    modifier = Modifier.alpha(alpha = skipAlpha),
                    enabled = !state.isLastPage
                ) {
                    Text(
                        text = "Saltar",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
                state.pages.getOrNull(pageIndex)?.let { page ->
                    OnboardingPageContent(page = page, pagerState = pagerState, pageIndex = pageIndex)
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 24.dp)
            ) {
                PageIndicator(pagerState = pagerState, pageCount = state.pageCount)

                Button(
                    onClick = { onAction(OnboardingAction.OnNextPage) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (state.isLastPage) "¡Empezar!" else "Siguiente",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPageDataUiModel, pagerState: PagerState, pageIndex: Int) {
    val pageOffset = (pagerState.currentPage - pageIndex + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
    val contentAlpha by animateFloatAsState(targetValue = 1f - pageOffset, animationSpec = tween(durationMillis = 200), label = "contentAlpha")
    val contentScale by animateFloatAsState(targetValue = 1f - (pageOffset * 0.08f), animationSpec = tween(durationMillis = 200), label = "contentScale")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .alpha(contentAlpha)
            .scale(contentScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            IllustrationPlaceholder(image = page.image, color = page.illustrationColor())
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun PageIndicator(pagerState: PagerState, pageCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(times = pageCount) { index ->
            val isSelected = pagerState.currentPage == index
            val dotWidth by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 8.dp,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "dotWidth_$index"
            )
            val dotColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                animationSpec = tween(durationMillis = 300),
                label = "dotColor_$index"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Composable
private fun IllustrationPlaceholder(image: String, color: Color) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = image, style = MaterialTheme.typography.displayLarge)
    }
}
