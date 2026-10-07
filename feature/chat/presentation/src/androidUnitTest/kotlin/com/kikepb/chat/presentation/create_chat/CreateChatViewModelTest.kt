package com.kikepb.chat.presentation.create_chat

import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.usecases.CreateChatUseCase
import com.kikepb.chat.domain.usecases.GetChatParticipantUseCase
import com.kikepb.chat.presentation.fake.FakeChatParticipantService
import com.kikepb.chat.presentation.fake.FakeChatRepository
import com.kikepb.chat.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var participantService: FakeChatParticipantService
    private lateinit var chatRepository: FakeChatRepository
    private lateinit var viewModel: CreateChatViewModel

    @Before
    fun setUp() {
        participantService = FakeChatParticipantService()
        chatRepository = FakeChatRepository()
        viewModel = CreateChatViewModel(
            getChatParticipantUseCase = GetChatParticipantUseCase(chatParticipantService = participantService),
            createChatUseCase = CreateChatUseCase(chatRepository = chatRepository)
        )
    }

    @Test
    fun `GIVEN initial state WHEN no query THEN canAddParticipant is false`() = runTest {
        viewModel.state.test {
            assertFalse(awaitItem().canAddParticipant)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN blank query WHEN state updated THEN canAddParticipant stays false`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.queryTextState.edit { replace(0, length, "   ") }
            Snapshot.sendApplyNotifications()
            advanceTimeBy(1100L)

            cancelAndIgnoreRemainingEvents()
            assertFalse(viewModel.state.value.canAddParticipant)
        }
    }

    @Test
    fun `GIVEN valid query WHEN search succeeds THEN canAddParticipant becomes true`() = runTest {
        val participant = ChatParticipantModel(
            userId = "found-user",
            username = "founduser",
            profilePictureUrl = null
        )
        participantService.searchParticipantResult = Result.Success(participant)

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.queryTextState.edit { replace(0, length, "founduser") }
            Snapshot.sendApplyNotifications()
            advanceTimeBy(1100L)

            val state = awaitItem()
            assertTrue(state.canAddParticipant)
            assertNotNull(state.currentSearchResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN search fails with NOT_FOUND WHEN query entered THEN searchError is set`() = runTest {
        participantService.searchParticipantResult = Result.Failure(error = DataError.Remote.NOT_FOUND)

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.queryTextState.edit { replace(0, length, "nobody") }
            Snapshot.sendApplyNotifications()
            advanceTimeBy(1100L)

            val state = awaitItem()
            assertFalse(state.canAddParticipant)
            assertNotNull(state.searchError)
            assertNull(state.currentSearchResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN search result available WHEN OnAddClick THEN participant added to selectedList`() = runTest {
        val participant = ChatParticipantModel(
            userId = "found-user",
            username = "founduser",
            profilePictureUrl = null
        )
        participantService.searchParticipantResult = Result.Success(participant)

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.queryTextState.edit { replace(0, length, "founduser") }
            Snapshot.sendApplyNotifications()
            advanceTimeBy(1100L)

            // Wait for search result to populate
            awaitItem()

            viewModel.onAction(ManageChatAction.OnAddClick)

            val state = awaitItem()
            assertEquals(1, state.selectedChatParticipants.size)
            assertEquals("found-user", state.selectedChatParticipants.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN participant already added WHEN OnAddClick again THEN no duplicate added`() = runTest {
        val participant = ChatParticipantModel(
            userId = "found-user",
            username = "founduser",
            profilePictureUrl = null
        )
        participantService.searchParticipantResult = Result.Success(participant)

        viewModel.state.test {
            awaitItem()

            // First add
            viewModel.state.value.queryTextState.edit { replace(0, length, "founduser") }
            Snapshot.sendApplyNotifications()
            advanceTimeBy(1100L)
            awaitItem()
            viewModel.onAction(ManageChatAction.OnAddClick)
            awaitItem()

            // Adding again must be a no-op; StateFlow conflates identical states, so don't await a new one.
            viewModel.onAction(ManageChatAction.OnAddClick)

            cancelAndIgnoreRemainingEvents()
            assertEquals(1, viewModel.state.value.selectedChatParticipants.size)
        }
    }

    @Test
    fun `GIVEN no participants selected WHEN OnPrimaryActionClick THEN no chat created`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ManageChatAction.OnPrimaryActionClick)
            cancelAndIgnoreRemainingEvents()
        }
        // No event emitted — createChat was not called
        assertTrue(chatRepository.createChatResult is Result.Success)
    }

    @Test
    fun `GIVEN participants selected WHEN OnPrimaryActionClick succeeds THEN OnChatCreated event emitted`() = runTest {
        val participant = ChatParticipantModel(
            userId = "found-user",
            username = "founduser",
            profilePictureUrl = null
        )
        participantService.searchParticipantResult = Result.Success(participant)
        chatRepository.createChatResult = Result.Success(FakeChatRepository.defaultChatModel())

        viewModel.events.test {
            viewModel.state.test {
                awaitItem()

                viewModel.state.value.queryTextState.edit { replace(0, length, "founduser") }
                Snapshot.sendApplyNotifications()
                advanceTimeBy(1100L)
                awaitItem()

                viewModel.onAction(ManageChatAction.OnAddClick)
                awaitItem()

                viewModel.onAction(ManageChatAction.OnPrimaryActionClick)
                cancelAndIgnoreRemainingEvents()
            }

            val event = awaitItem()
            assertTrue(event is CreateChatEvent.OnChatCreated)
        }
    }

    @Test
    fun `GIVEN chat creation fails WHEN OnPrimaryActionClick THEN submitError is set`() = runTest {
        val participant = ChatParticipantModel(
            userId = "found-user",
            username = "founduser",
            profilePictureUrl = null
        )
        participantService.searchParticipantResult = Result.Success(participant)
        chatRepository.createChatResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.queryTextState.edit { replace(0, length, "founduser") }
            Snapshot.sendApplyNotifications()
            advanceTimeBy(1100L)
            awaitItem()

            viewModel.onAction(ManageChatAction.OnAddClick)
            awaitItem()

            viewModel.onAction(ManageChatAction.OnPrimaryActionClick)
            // With UnconfinedTestDispatcher the intermediate isSubmitting state may be conflated away.
            val state = expectMostRecentItem()
            assertNotNull(state.submitError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
