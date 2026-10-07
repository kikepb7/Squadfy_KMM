package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.JoinClubError
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.core.domain.util.mapError

/** BE-001 RN-3/RN-6: alphanumeric code (case-insensitive), optional shirt 1-999 and a position from the enum. */
class JoinClubUseCase(private val clubRepository: ClubRepository) {

    suspend operator fun invoke(invitationCode: String, shirtNumber: String?, position: PlayerPosition?): Result<ClubModel, JoinClubError> {
        val code = invitationCode.trim().uppercase()
        if (!code.matches(INVITATION_CODE_REGEX)) return Failure(JoinClubError.InvalidInvitationCodeFormat)

        val parsedShirtNumber = when {
            shirtNumber.isNullOrBlank() -> null
            else -> shirtNumber.trim().toIntOrNull()?.takeIf { it in VALID_SHIRT_NUMBERS }
                ?: return Failure(JoinClubError.InvalidShirtNumber)
        }

        return clubRepository.joinClub(invitationCode = code, shirtNumber = parsedShirtNumber, position = position)
            .mapError { JoinClubError.Remote(it) }
    }

    companion object {
        private val INVITATION_CODE_REGEX = Regex("^[A-Z0-9]{6,12}$")
        val VALID_SHIRT_NUMBERS = 1..999
    }
}
