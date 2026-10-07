package com.kikepb.chat.presentation.chat_list

import app.cash.turbine.test
import com.kikepb.chat.domain.usecases.DeleteAllChatsUseCase
import com.kikepb.chat.domain.usecases.FetchChatsUseCase
import com.kikepb.chat.domain.usecases.GetChatsUseCase
import com.kikepb.chat.domain.usecases.LogoutUseCase
import com.kikepb.chat.domain.usecases.UnregisterTokenUseCase
import com.kikepb.chat.domain.usecases.profile.FetchLocalUserProfileUseCase
import com.kikepb.chat.presentation.fake.FakeAuthRepository
import com.kikepb.chat.presentation.fake.FakeChatParticipantRepository
import com.kikepb.chat.presentation.fake.FakeChatRepository
import com.kikepb.chat.presentation.fake.FakeDeviceTokenService
import com.kikepb.chat.presentation.fake.FakeSessionStorage
import com.kikepb.chat.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var chatRepository: FakeChatRepository
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var deviceTokenService: FakeDeviceTokenService
    private lateinit var participantRepository: FakeChatParticipantRepository

    @Before
    fun setUp() {
        chatRepository = FakeChatRepository()
        sessionStorage = FakeSessionStorage()
        authRepository = FakeAuthRepository()
        deviceTokenService = FakeDeviceTokenService()
        participantRepository = FakeChatParticipantRepository()
        // ChatListViewModel resets its state while there is no session, so UI actions need a logged-in user.
        runBlocking { sessionStorage.set(loggedInAuthInfo()) }
    }

    private fun loggedInAuthInfo() = AuthInfoModel(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        user = UserModel(
            id = "user-id",
            email = "user@example.com",
            username = "testuser",
            hasVerifiedEmail = true,
            profilePictureUrl = null
        )
    )

    private fun createViewModel() = ChatListViewModel(
        getChatsUseCase = GetChatsUseCase(chatRepository = chatRepository),
        fetchChatsUseCase = FetchChatsUseCase(chatRepository = chatRepository),
        sessionStorage = sessionStorage,
        logoutUseCase = LogoutUseCase(authRepository = authRepository),
        unregisterTokenUseCase = UnregisterTokenUseCase(deviceTokenService = deviceTokenService),
        deleteAllChatsUseCase = DeleteAllChatsUseCase(chatRepository = chatRepository),
        fetchLocalUserProfileUseCase = FetchLocalUserProfileUseCase(chatParticipantRepository = participantRepository)
    )

    @Test
    fun `GIVEN logged in user and no chats WHEN collecting state THEN chats is empty`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.chats.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN user avatar clicked WHEN OnUserAvatarClick THEN isUserMenuOpen becomes true`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatListAction.OnUserAvatarClick)
            val state = awaitItem()
            assertTrue(state.isUserMenuOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN user menu open WHEN OnDismissUserMenu THEN isUserMenuOpen becomes false`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatListAction.OnUserAvatarClick)
            awaitItem()
            viewModel.onAction(ChatListAction.OnDismissUserMenu)
            val state = awaitItem()
            assertFalse(state.isUserMenuOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN user menu open WHEN OnLogoutClick THEN showLogoutConfirmation becomes true and menu closes`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatListAction.OnUserAvatarClick)
            awaitItem()
            viewModel.onAction(ChatListAction.OnLogoutClick)
            val state = awaitItem()
            assertTrue(state.showLogoutConfirmation)
            assertFalse(state.isUserMenuOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN logout dialog shown WHEN OnDismissLogoutDialog THEN showLogoutConfirmation becomes false`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatListAction.OnLogoutClick)
            awaitItem()
            viewModel.onAction(ChatListAction.OnDismissLogoutDialog)
            val state = awaitItem()
            assertFalse(state.showLogoutConfirmation)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN user confirms logout WHEN OnConfirmLogout THEN OnLogoutSuccess event is emitted`() = runTest {
        sessionStorage.set(FakeAuthRepository.defaultAuthInfoModel())
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(ChatListAction.OnConfirmLogout)
            val event = awaitItem()
            assertTrue(event is ChatListEvent.OnLogoutSuccess)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN user confirms logout WHEN OnConfirmLogout THEN session is cleared`() = runTest {
        sessionStorage.set(FakeAuthRepository.defaultAuthInfoModel())
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(ChatListAction.OnConfirmLogout)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertNull(sessionStorage.savedInfo)
    }

    @Test
    fun `GIVEN user confirms logout WHEN OnConfirmLogout THEN all chats are deleted`() = runTest {
        sessionStorage.set(FakeAuthRepository.defaultAuthInfoModel())
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(ChatListAction.OnConfirmLogout)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(chatRepository.deleteAllChatsCalled)
    }

    @Test
    fun `GIVEN chat selected WHEN OnSelectChat THEN selectedChatId is updated`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatListAction.OnSelectChat(chatId = "chat-1"))
            val state = awaitItem()
            assertEquals("chat-1", state.selectedChatId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN profile settings clicked WHEN OnProfileSettingsClick THEN isUserMenuOpen becomes false`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatListAction.OnUserAvatarClick)
            awaitItem()
            viewModel.onAction(ChatListAction.OnProfileSettingsClick)
            val state = awaitItem()
            assertFalse(state.isUserMenuOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
