@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.kikepb.chat.presentation.manage_chat

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.usecases.AddParticipantsToChatUseCase
import com.kikepb.chat.domain.usecases.GetActiveParticipantsByChatIdUseCase
import com.kikepb.chat.domain.usecases.GetChatParticipantUseCase
import com.kikepb.chat.presentation.create_chat.ManageChatAction
import com.kikepb.chat.presentation.create_chat.ManageChatState
import com.kikepb.chat.presentation.manage_chat.ManageChatEvent.OnMembersAdded
import com.kikepb.chat.presentation.mappers.toUi
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.error_participant_not_found
import kotlin.time.Duration.Companion.seconds

class ManageChatViewModel(
    private val addParticipantsToChatUseCase: AddParticipantsToChatUseCase,
    private val getChatParticipantUseCase: GetChatParticipantUseCase,
    private val getActiveParticipantsByChatIdUseCase: GetActiveParticipantsByChatIdUseCase
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val _chatId = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(ManageChatState())
    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                getActiveParticipantsByChatIdUseCase.getActiveParticipantsByChatId(chatId = chatId)
            } else emptyFlow()
        }
        .combine(flow = _state) { participants, currentState ->
            currentState.copy(existingChatParticipants = participants.map { it.toUi() })
        }
        .onStart {
            if (!hasLoadedInitialData) {
                searchFlow.launchIn(viewModelScope)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ManageChatState()
        )
    private val eventChannel = Channel<ManageChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private val searchFlow = snapshotFlow { _state.value.queryTextState.text.toString() }
        .debounce(timeout = 1.seconds)
        .onEach { query ->
            performSearch(query = query)
        }

    private fun ManageChatViewModel.performSearch(query: String) {
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
        state.value.currentSearchResult?.let { participantFromSearch ->
            val isAlreadySelected = state.value.selectedChatParticipants.any {
                it.id == participantFromSearch.id
            }
            val isAlreadyInChat = state.value.existingChatParticipants.any {
                it.id == participantFromSearch.id
            }
            val updatedParticipants =
                if (isAlreadyInChat || isAlreadySelected) state.value.selectedChatParticipants
                else state.value.selectedChatParticipants + participantFromSearch


            state.value.queryTextState.clearText()

            _state.update { it.copy(
                selectedChatParticipants = updatedParticipants,
                canAddParticipant = false,
                currentSearchResult = null
            ) }
        }
    }
    private fun addParticipantsToChat() {
        if (state.value.selectedChatParticipants.isEmpty()) return

        val chatId = _chatId.value ?: return

        val selectedParticipants = state.value.selectedChatParticipants
        val selectedUserIds = selectedParticipants.map { it.id }

        viewModelScope.launch {
            addParticipantsToChatUseCase.addParticipantsToChat(chatId = chatId, userIds = selectedUserIds)
                .onSuccess { eventChannel.send(element = OnMembersAdded) }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, submitError = error.toUiText()) }
                }
        }
    }

    fun onAction(action: ManageChatAction) {
        when (action) {
            ManageChatAction.OnAddClick -> addParticipant()
            ManageChatAction.OnPrimaryActionClick -> addParticipantsToChat()
            is ManageChatAction.ChatParticipants.OnSelectChat -> {
                _chatId.update { action.chatId }
            }
            else -> Unit
        }
    }
}

sealed interface ManageChatEvent {
    data object OnMembersAdded: ManageChatEvent
}