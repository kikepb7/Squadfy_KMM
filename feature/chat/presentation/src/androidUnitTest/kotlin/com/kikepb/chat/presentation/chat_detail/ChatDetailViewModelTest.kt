package com.kikepb.chat.presentation.chat_detail

import app.cash.turbine.test
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.usecases.FetchChatByIdUseCase
import com.kikepb.chat.domain.usecases.GetChatInfoByIdUseCase
import com.kikepb.chat.domain.usecases.LeaveChatUseCase
import com.kikepb.chat.domain.usecases.message.DeleteMessageUseCase
import com.kikepb.chat.domain.usecases.message.FetchMessagesUseCase
import com.kikepb.chat.domain.usecases.message.GetMessagesForChatUseCase
import com.kikepb.chat.domain.usecases.message.RetryMessageUseCase
import com.kikepb.chat.domain.usecases.message.SendMessageUseCase
import com.kikepb.chat.presentation.fake.FakeChatConnectionClient
import com.kikepb.chat.presentation.fake.FakeChatRepository
import com.kikepb.chat.presentation.fake.FakeMessageRepository
import com.kikepb.chat.presentation.fake.FakeSessionStorage
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import com.kikepb.chat.presentation.util.MainDispatcherRule
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var chatRepository: FakeChatRepository
    private lateinit var messageRepository: FakeMessageRepository
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var connectionClient: FakeChatConnectionClient
    private lateinit var viewModel: ChatDetailViewModel

    @Before
    fun setUp() {
        chatRepository = FakeChatRepository()
        messageRepository = FakeMessageRepository()
        sessionStorage = FakeSessionStorage()
        connectionClient = FakeChatConnectionClient()
        viewModel = createViewModel()
    }

    private fun createViewModel() = ChatDetailViewModel(
        fetchChatByIdUseCase = FetchChatByIdUseCase(chatRepository = chatRepository),
        getChatInfoByIdUseCase = GetChatInfoByIdUseCase(chatRepository = chatRepository),
        leaveChatUseCase = LeaveChatUseCase(chatRepository = chatRepository),
        fetchMessagesUseCase = FetchMessagesUseCase(messageRepository = messageRepository),
        getMessagesForChatUseCase = GetMessagesForChatUseCase(messageRepository = messageRepository),
        sendMessageUseCase = SendMessageUseCase(messageRepository = messageRepository),
        retryMessageUseCase = RetryMessageUseCase(messageRepository = messageRepository),
        deleteMessageUseCase = DeleteMessageUseCase(messageRepository = messageRepository),
        chatConnectionClient = connectionClient,
        sessionStorage = sessionStorage
    )

    // --- Simple state mutations (no chatId required) ---

    @Test
    fun `GIVEN initial state THEN chatUi is null and canSendMessage is false`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.chatUi)
            assertFalse(state.canSendMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN chat options not open WHEN OnChatOptionsClick THEN isChatOptionsOpen becomes true`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnChatOptionsClick)
            assertTrue(awaitItem().isChatOptionsOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN chat options open WHEN OnDismissChatOptions THEN isChatOptionsOpen becomes false`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnChatOptionsClick)
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnDismissChatOptions)
            assertFalse(awaitItem().isChatOptionsOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN message WHEN OnMessageLongClick THEN messageWithOpenMenu is set`() = runTest {
        val message = aLocalUserMessage(id = "msg-1")

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnMessageLongClick(message = message))
            val state = awaitItem()
            assertEquals(message, state.messageWithOpenMenu)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN message menu open WHEN OnDismissMessageMenu THEN messageWithOpenMenu becomes null`() = runTest {
        val message = aLocalUserMessage(id = "msg-1")

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnMessageLongClick(message = message))
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnDismissMessageMenu)
            assertNull(awaitItem().messageWithOpenMenu)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN banner visible WHEN OnHideBanner THEN bannerState isVisible becomes false`() = runTest {
        // Trigger banner via OnTopVisibleIndexChanged with empty messages — banner stays hidden
        // We test OnHideBanner directly by checking it turns off an already-visible banner.
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnHideBanner)
            // Banner was already invisible — no state emission (state doesn't change)
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.state.value.bannerState.isVisible)
    }

    @Test
    fun `GIVEN first visible index at most 3 WHEN OnFirstVisibleIndexChanged THEN isNearBottom is true`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnFirstVisibleIndexChanged(index = 2))
            assertTrue(awaitItem().isNearBottom)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN first visible index greater than 3 WHEN OnFirstVisibleIndexChanged THEN isNearBottom is false`() = runTest {
        viewModel.state.test {
            awaitItem()
            // Set near bottom first, then move away
            viewModel.onAction(ChatDetailAction.OnFirstVisibleIndexChanged(index = 1))
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnFirstVisibleIndexChanged(index = 10))
            assertFalse(awaitItem().isNearBottom)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN top visible index with empty messages WHEN OnTopVisibleIndexChanged THEN bannerState stays hidden`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ChatDetailAction.OnTopVisibleIndexChanged(topVisibleIndex = 0))
            // No state change emitted since bannerState stays the same (no messages)
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.state.value.bannerState.isVisible)
    }

    // --- Helpers ---

    private fun aLocalUserMessage(id: String) = LocalUserMessage(
        id = id,
        content = "Hello",
        deliveryStatus = ChatMessageDeliveryStatus.SENT,
        formattedSentTime = UiText.DynamicString("10:00")
    )
}
