package com.kikepb.chat.presentation.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.usecases.DeleteAllChatsUseCase
import com.kikepb.chat.domain.usecases.FetchChatsUseCase
import com.kikepb.chat.domain.usecases.GetChatsUseCase
import com.kikepb.chat.domain.usecases.LogoutUseCase
import com.kikepb.chat.domain.usecases.UnregisterTokenUseCase
import com.kikepb.chat.domain.usecases.profile.FetchLocalUserProfileUseCase
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnConfirmLogout
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnDismissLogoutDialog
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnDismissUserMenu
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnLogoutClick
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnProfileSettingsClick
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnSelectChat
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnUserAvatarClick
import com.kikepb.chat.presentation.chat_list.ChatListEvent.OnLogoutError
import com.kikepb.chat.presentation.chat_list.ChatListEvent.OnLogoutSuccess
import com.kikepb.chat.presentation.mappers.toUi
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val getChatsUseCase: GetChatsUseCase,
    private val fetchChatsUseCase: FetchChatsUseCase,
    private val sessionStorage: SessionStorage,
    private val logoutUseCase: LogoutUseCase,
    private val unregisterTokenUseCase: UnregisterTokenUseCase,
    private val deleteAllChatsUseCase: DeleteAllChatsUseCase,
    private val fetchLocalUserProfileUseCase: FetchLocalUserProfileUseCase
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ChatListEvent>()
    val events = eventChannel.receiveAsFlow()
    private val _state = MutableStateFlow(ChatListState())
    val state = combine(
        _state,
        getChatsUseCase.getChats(),
        sessionStorage.observeAuthInfo()
    ) { currentState, chats, authInfo ->
        if (authInfo == null) return@combine ChatListState()

        currentState.copy(
            chats = chats.map { it.toUi(localParticipantId = authInfo.user.id) },
            localParticipant = authInfo.user.toUi()
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                loadChats()
                fetchLocalUserProfile()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatListState()
        )

    private fun loadChats() = viewModelScope.launch { fetchChatsUseCase.fetchChats() }

    private fun showLogoutConfirmation() = _state.update { it.copy(isUserMenuOpen = false, showLogoutConfirmation = true) }

    private fun logout() {
        _state.update { it.copy(showLogoutConfirmation = false) }

        viewModelScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().first()
            val refreshToken = authInfo?.refreshToken ?: return@launch

            unregisterTokenUseCase.unregisterToken(token = refreshToken)
                .onSuccess {
                    logoutUseCase.logout(refreshToken = refreshToken)
                        .onSuccess {
                            sessionStorage.set(info = null)
                            deleteAllChatsUseCase.deleteAllChats()
                            eventChannel.send(element = OnLogoutSuccess)
                        }
                        .onFailure { error ->
                        eventChannel.send(element = OnLogoutError(error = error.toUiText())) }
                }
                .onFailure { error ->
                    eventChannel.send(element = OnLogoutError(error = error.toUiText()))
                }
        }
    }

    private fun fetchLocalUserProfile() {
        viewModelScope.launch {
            fetchLocalUserProfileUseCase.fetchLocalUserProfile()
        }
    }

    fun onAction(action: ChatListAction) {
        when (action) {
            is OnSelectChat -> { _state.update { it.copy(selectedChatId = action.chatId) } }
            OnUserAvatarClick -> { _state.update { it.copy(isUserMenuOpen = true) } }
            OnLogoutClick -> showLogoutConfirmation()
            OnConfirmLogout -> logout()
            OnDismissLogoutDialog -> _state.update { it.copy(showLogoutConfirmation = false)}
            OnProfileSettingsClick, OnDismissUserMenu -> { _state.update { it.copy(isUserMenuOpen = false) } }
            else -> Unit
        }
    }
}

data class ChatListState(
    val chats: List<ChatModelUi> = emptyList(),
    val error: UiText? = null,
    val localParticipant: ChatParticipantModelUi? = null,
    val isUserMenuOpen: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val selectedChatId: String? = null,
    val isLoading: Boolean = false,
)

sealed interface ChatListEvent {
    data object OnLogoutSuccess: ChatListEvent
    data class OnLogoutError(val error: UiText): ChatListEvent
}

sealed interface ChatListAction {
    data object OnUserAvatarClick: ChatListAction
    data object OnDismissUserMenu: ChatListAction
    data object OnLogoutClick: ChatListAction
    data object OnConfirmLogout: ChatListAction
    data object OnDismissLogoutDialog: ChatListAction
    data object OnCreateChatClick: ChatListAction
    data object OnProfileSettingsClick: ChatListAction
    data class OnSelectChat(val chatId: String?): ChatListAction
}