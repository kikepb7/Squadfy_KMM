package com.kikepb.club.presentation.memberdetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.club.domain.usecase.BanMemberUseCase
import com.kikepb.club.domain.usecase.ChangeMemberRoleUseCase
import com.kikepb.club.domain.usecase.GetClubMemberByIdUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.RemoveMemberUseCase
import com.kikepb.club.domain.usecase.TransferOwnershipUseCase
import com.kikepb.club.domain.usecase.UpdateMyClubPictureUseCase
import com.kikepb.club.domain.usecase.UpdateMyMembershipUseCase
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.member
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.member_club_picture_invalid
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MemberClubPictureTest {

    private val repository = FakeClubRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(): MemberDetailViewModel {
        repository.members.value = listOf(member("m-me", userId = "me").copy(profilePictureUrl = "https://cdn/profile.jpg"))
        return MemberDetailViewModel(
            getClubMemberByIdUseCase = GetClubMemberByIdUseCase(repository),
            observeMyMembershipUseCase = ObserveMyMembershipUseCase(repository, FakeSessionStorage("me")),
            changeMemberRoleUseCase = ChangeMemberRoleUseCase(repository),
            removeMemberUseCase = RemoveMemberUseCase(repository),
            banMemberUseCase = BanMemberUseCase(repository),
            transferOwnershipUseCase = TransferOwnershipUseCase(repository),
            updateMyMembershipUseCase = UpdateMyMembershipUseCase(repository),
            updateMyClubPictureUseCase = UpdateMyClubPictureUseCase(repository),
            savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1", "memberId" to "m-me"))
        )
    }

    private fun TestScope.collect(viewModel: MemberDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
    }

    @Test
    fun `AC-015-05 the club picture replaces the profile picture and can be removed`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        collect(viewModel)
        assertEquals("https://cdn/profile.jpg", viewModel.state.value.member?.pictureUrl)

        viewModel.onAction(MemberDetailAction.OnClubPicturePicked(bytes = byteArrayOf(1, 2), mimeType = "image/png"))
        assertEquals(listOf("image/png"), repository.pictureUploads)
        assertEquals("https://cdn/club/club-1/me.jpg", viewModel.state.value.member?.pictureUrl)

        viewModel.onAction(MemberDetailAction.OnRemoveClubPicture)
        assertNull(viewModel.state.value.member?.clubPictureUrl)
        assertEquals("https://cdn/profile.jpg", viewModel.state.value.member?.pictureUrl)
    }

    @Test
    fun `AC-015-05 formats the backend rejects are not uploaded`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        collect(viewModel)

        viewModel.events.test {
            viewModel.onAction(MemberDetailAction.OnClubPicturePicked(bytes = byteArrayOf(1), mimeType = "image/gif"))

            val event = awaitItem() as MemberDetailEvent.ShowMessage
            assertEquals(Res.string.member_club_picture_invalid, (event.message as UiText.Resource).id)
        }
        assertTrue(repository.pictureUploads.isEmpty())
    }
}
