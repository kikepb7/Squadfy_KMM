package com.kikepb.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.onboarding.domain.usecase.SetHasSeenOnboardingUseCase
import com.kikepb.onboarding.presentation.OnboardingAction.OnNextPage
import com.kikepb.onboarding.presentation.OnboardingAction.OnPageChanged
import com.kikepb.onboarding.presentation.OnboardingAction.OnSkip
import com.kikepb.onboarding.presentation.model.OnboardingPageDataUiModel
import com.kikepb.onboarding.presentation.model.OnboardingPageDataUiModel.Companion.pages
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val setHasSeenOnboardingUseCase: SetHasSeenOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()
    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnPageChanged -> _state.update { it.copy(currentPage = action.page) }

            OnNextPage -> {
                if (_state.value.isLastPage) finishOnboarding()
                else _state.update { it.copy(currentPage = it.currentPage + 1) }
            }

            OnSkip -> finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        viewModelScope.launch {
            setHasSeenOnboardingUseCase()
            _events.send(element = OnboardingEvent.OnFinished)
        }
    }
}

data class OnboardingState(
    val pages: List<OnboardingPageDataUiModel> = pages(),
    val currentPage: Int = 0
) {
    val pageCount: Int get() = pages.size
    val isLastPage: Boolean get() = currentPage == pages.lastIndex
}

sealed interface OnboardingEvent {
    data object OnFinished : OnboardingEvent
}

sealed interface OnboardingAction {
    data class OnPageChanged(val page: Int) : OnboardingAction
    data object OnNextPage : OnboardingAction
    data object OnSkip : OnboardingAction
}
