package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.fake.FakeClubRepository
import com.kikepb.club.domain.fake.club
import com.kikepb.club.domain.fake.member
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.CreateClubError
import com.kikepb.club.domain.model.EditClubError
import com.kikepb.club.domain.model.JoinClubError
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClubMembershipUseCasesTest {

    private val repository = FakeClubRepository()

    private class FakeSessionStorage(userId: String) : SessionStorage {
        private val info = MutableStateFlow<AuthInfoModel?>(
            AuthInfoModel("a", "r", UserModel(id = userId, email = "e", username = "u", hasVerifiedEmail = true, profilePictureUrl = null))
        )
        override fun observeAuthInfo(): Flow<AuthInfoModel?> = info
        override suspend fun set(info: AuthInfoModel?) { this.info.value = info }
    }

    @Test
    fun `AC-003-04 join normalizes the code to upper case and sends the typed position`() = runTest {
        val result = JoinClubUseCase(repository)(invitationCode = " ab12cd34 ", shirtNumber = "7", position = PlayerPosition.DEFENDER)

        assertTrue(result is Result.Success)
        assertEquals(FakeClubRepository.JoinCall("AB12CD34", 7, PlayerPosition.DEFENDER), repository.joinCalls.single())
    }

    @Test
    fun `AC-003-04 join validates the code format and the shirt number 1-999 locally`() = runTest {
        val join = JoinClubUseCase(repository)

        assertEquals(Result.Failure(JoinClubError.InvalidInvitationCodeFormat), join("abc", null, null))
        assertEquals(Result.Failure(JoinClubError.InvalidShirtNumber), join("AB12CD34", "1000", null))
        assertEquals(Result.Failure(JoinClubError.InvalidShirtNumber), join("AB12CD34", "0", null))
        assertTrue(join("AB12CD34", "999", null) is Result.Success)
        assertEquals(1, repository.joinCalls.size)
    }

    @Test
    fun `AC-003-04 join backend errors stay typed`() = runTest {
        repository.joinResult = Result.Failure(ClubError.BannedFromClub)

        assertEquals(Result.Failure(JoinClubError.Remote(ClubError.BannedFromClub)), JoinClubUseCase(repository)("AB12CD34", null, null))
    }

    @Test
    fun `AC-003-03 a failed logo upload keeps the created club`() = runTest {
        repository.logoResult = Result.Failure(ClubError.Remote(com.kikepb.core.domain.util.RemoteError(com.kikepb.core.domain.util.DataError.Remote.SERVER_ERROR)))

        val result = CreateClubUseCase(repository)("Squadfy FC", null, null, logoBytes = byteArrayOf(1), logoMimeType = "image/png")

        val created = (result as Result.Success).data
        assertEquals("club-1", created.club.id)
        assertTrue(created.logoUploadFailed)
        assertEquals(1, repository.logoUploads)
    }

    @Test
    fun `AC-003-11 create and edit validate name, description and member limit`() = runTest {
        val create = CreateClubUseCase(repository)
        val edit = EditClubUseCase(repository)

        assertEquals(Result.Failure(CreateClubError.BlankName), create("  ", null, null, null, null))
        assertEquals(Result.Failure(CreateClubError.NameTooLong), create("x".repeat(121), null, null, null, null))
        assertEquals(Result.Failure(CreateClubError.DescriptionTooLong), create("ok", "d".repeat(2001), null, null, null))
        assertEquals(Result.Failure(CreateClubError.InvalidMaxMembers), create("ok", null, "0", null, null))
        assertEquals(Result.Failure(EditClubError.InvalidMaxMembers), edit("club-1", "ok", null, "-3"))
        assertTrue(edit("club-1", " New name ", "desc", "20") is Result.Success)
        assertEquals(Triple("New name", "desc", 20), repository.editCalls.single())
    }

    @Test
    fun `AC-003-11 maxMembers below current members is surfaced as a typed edit error`() = runTest {
        repository.editResult = Result.Failure(ClubError.MaxMembersBelowCurrent)

        assertEquals(
            Result.Failure(EditClubError.Remote(ClubError.MaxMembersBelowCurrent)),
            EditClubUseCase(repository)("club-1", "ok", null, "2")
        )
    }

    @Test
    fun `AC-003-01 my clubs carry my role when the members are cached`() = runTest {
        repository.clubs.value = listOf(club("club-1"), club("club-2"))
        repository.members.value = listOf(member("m-1", userId = "me", clubId = "club-1", role = ClubMemberRole.ADMIN))

        val myClubs = ObserveMyClubsUseCase(repository, FakeSessionStorage("me"))().first()

        assertEquals(ClubMemberRole.ADMIN, myClubs.first { it.club.id == "club-1" }.myRole)
        assertEquals(null, myClubs.first { it.club.id == "club-2" }.myRole)
    }

    @Test
    fun `AC-003-06 my membership is the member whose user is the session user`() = runTest {
        repository.members.value = listOf(member("m-1", userId = "other"), member("m-2", userId = "me", role = ClubMemberRole.OWNER))

        val me = ObserveMyMembershipUseCase(repository, FakeSessionStorage("me"))("club-1").first()

        assertEquals("m-2", me?.id)
    }
}
