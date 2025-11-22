package com.kikepb.chat.presentation.create_chat

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.usecases.GetChatParticipantUseCase
import com.kikepb.chat.presentation.create_chat.model.ChatParticipantUiModel
import com.kikepb.chat.presentation.mappers.toUi
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.error_participant_not_found
import kotlin.time.Duration.Companion.seconds

@FlowPreview
class CreateChatViewModel(
    private val getChatParticipantUseCase: GetChatParticipantUseCase
) : ViewModel() {

    private var hasLoadedInitialData = false


    private val _state = MutableStateFlow(CreateChatState())
    private val searchFlow = snapshotFlow { _state.value.queryTextState.text.toString() }
        .debounce(timeout = 1.seconds)
        .onEach { query ->
            performSearch(query = query)
        }
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                searchFlow.launchIn(viewModelScope)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CreateChatState()
        )


    private fun CreateChatViewModel.performSearch(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(currentSearchResult = null, canAddParticipant = false, searchError = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, canAddParticipant = false) }

            getChatParticipantUseCase.invoke(query = query)
                .onSuccess { participant ->
                    _state.update {
                        it.copy(
                            currentSearchResult = participant.toUi(),
                            isSearching = false,
                            canAddParticipant = true,
                            searchError = null
                        )
                    }
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        DataError.Remote.NOT_FOUND -> UiText.Resource(RString.error_participant_not_found)
                        else -> error.toUiText()
                    }

                    _state.update {
                        it.copy(
                            searchError = errorMessage,
                            isSearching = false,
                            canAddParticipant = false,
                            currentSearchResult = null
                        )
                    }
                }
        }
    }

    private fun addParticipant() {
        state.value.currentSearchResult?.let { participant ->
            val isAlreadyPartOfChat = state.value.selectedChatParticipants.any {
                it.id == participant.id
            }

            if (!isAlreadyPartOfChat) {
                _state.update {
                    it.copy(
                        selectedChatParticipants = it.selectedChatParticipants + participant,
                        canAddParticipant = false,
                        currentSearchResult = null
                    )
                }

                _state.value.queryTextState.clearText()
            }
        }
    }

    fun onAction(action: CreateChatAction) {
        when (action) {
            CreateChatAction.OnAddClick -> addParticipant()
            CreateChatAction.OnCreateChatClick -> { }
            CreateChatAction.OnDismissDialog -> Unit
        }
    }
}

data class CreateChatState(
    val queryTextState: TextFieldState = TextFieldState(),
    val selectedChatParticipants: List<ChatParticipantUiModel> = emptyList(),
    val isSearching: Boolean = false,
    val canAddParticipant: Boolean = false,
    val currentSearchResult: ChatParticipantUiModel? = null,
    val searchError: UiText? = null,
    val isCreatingChat: Boolean = false
)

sealed interface CreateChatAction {
    data object OnAddClick: CreateChatAction
    data object OnDismissDialog: CreateChatAction
    data object OnCreateChatClick: CreateChatAction
}