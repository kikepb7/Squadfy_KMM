package com.kikepb.chat.presentation.create_chat

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.usecases.SearchChatParticipantsUseCase
import com.kikepb.chat.domain.usecases.CreateChatUseCase
import com.kikepb.chat.presentation.mappers.toUi
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
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

@OptIn(FlowPreview::class)
class CreateChatViewModel(
    private val searchChatParticipantsUseCase: SearchChatParticipantsUseCase,
    private val createChatUseCase: CreateChatUseCase,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val _state = MutableStateFlow(ManageChatState())
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
            initialValue = ManageChatState()
        )
    private val eventChannel = Channel<CreateChatEvent>()
    val events = eventChannel.receiveAsFlow()


    private fun CreateChatViewModel.performSearch(query: String) {
        if (query.trim().length < SearchChatParticipantsUseCase.MIN_QUERY_LENGTH) {
            _state.update { it.copy(searchResults = emptyList(), canAddParticipant = false, searchError = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, canAddParticipant = false) }

            searchChatParticipantsUseCase.invoke(query = query)
                .onSuccess { participants ->
                    val results = participants.map { it.toUi() }
                    _state.update {
                        it.copy(
                            searchResults = results,
                            isSearching = false,
                            canAddParticipant = results.isNotEmpty(),
                            searchError = if (results.isEmpty()) UiText.Resource(RString.error_participant_not_found) else null
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
                            searchResults = emptyList()
                        )
                    }
                }
        }
    }

    /** [OnAddClick] adds the best match (the backend lists prefix matches first); tapping a result adds that one. */
    private fun addParticipant(participant: ChatParticipantModelUi? = state.value.searchResults.firstOrNull()) {
        participant?.let { participant ->
            val isAlreadyPartOfChat = state.value.selectedChatParticipants.any {
                it.id == participant.id
            }

            if (!isAlreadyPartOfChat) {
                _state.update {
                    it.copy(
                        selectedChatParticipants = it.selectedChatParticipants + participant,
                        canAddParticipant = false,
                        searchResults = emptyList()
                    )
                }

                _state.value.queryTextState.clearText()
            }
        }
    }

    private fun createChat() {
        val userIds = state.value.selectedChatParticipants.map { it.id }

        if (userIds.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, canAddParticipant = false) }

            createChatUseCase.createChat(otherUserIds = userIds)
                .onSuccess { chat ->
                    _state.update { it.copy(isSubmitting = false) }
                    eventChannel.send(element = CreateChatEvent.OnChatCreated(chat = chat))
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        submitError = error.toUiText(),
                        canAddParticipant = it.searchResults.isNotEmpty() && !it.isSearching
                    ) }
                }
        }
    }

    fun onAction(action: ManageChatAction) {
        when (action) {
            ManageChatAction.OnAddClick -> addParticipant()
            is ManageChatAction.OnSearchResultClick -> addParticipant(participant = action.participant)
            ManageChatAction.OnPrimaryActionClick -> createChat()
            else -> Unit
        }
    }
}

data class ManageChatState(
    val queryTextState: TextFieldState = TextFieldState(),
    val existingChatParticipants: List<ChatParticipantModelUi> = emptyList(),
    val selectedChatParticipants: List<ChatParticipantModelUi> = emptyList(),
    val isSearching: Boolean = false,
    val canAddParticipant: Boolean = false,
    val searchResults: List<ChatParticipantModelUi> = emptyList(),
    val searchError: UiText? = null,
    val isSubmitting: Boolean = false,
    val submitError: UiText? = null
)

sealed interface CreateChatEvent {
    data class OnChatCreated(val chat: ChatModel) : CreateChatEvent
}

sealed interface ManageChatAction {
    data object OnAddClick: ManageChatAction
    data class OnSearchResultClick(val participant: ChatParticipantModelUi): ManageChatAction
    data object OnDismissDialog: ManageChatAction
    data object OnPrimaryActionClick: ManageChatAction

    sealed interface ChatParticipants: ManageChatAction {
        data class OnSelectChat(val chatId: String?): ManageChatAction
    }
}