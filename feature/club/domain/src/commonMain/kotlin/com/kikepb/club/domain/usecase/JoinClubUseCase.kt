package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.JoinClubError
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success

class JoinClubUseCase(private val clubRepository: ClubRepository) {

    suspend operator fun invoke(invitationCode: String, shirtNumber: String?, position: String?): Result<ClubModel, JoinClubError> {

        if (!isValidInvitationCode(invitationCode)) return Failure(JoinClubError.InvalidInvitationCodeFormat)

        val parsedShirtNumber = when (val result = parseShirtNumber(shirtNumber)) {
            is Success -> result.data
            is Failure -> return Failure(result.error)
        }

        return when (val result = clubRepository.joinClub(
            invitationCode = invitationCode,
            shirtNumber = parsedShirtNumber,
            position = position
        )) {
            is Success -> result
            is Failure -> Failure(JoinClubError.Remote(dataError = result.error))
        }
    }

    private fun isValidInvitationCode(code: String): Boolean = code.matches(INVITATION_CODE_REGEX)

    private fun parseShirtNumber(raw: String?): Result<Int?, JoinClubError> {
        if (raw.isNullOrBlank()) return Success(null)
        val n = raw.trim().toIntOrNull()
        return if (n != null && n in VALID_SHIRT_NUMBER_RANGE) Success(n)
        else Failure(JoinClubError.InvalidShirtNumber)
    }

    companion object {
        private val INVITATION_CODE_REGEX = Regex("^[A-Za-z0-9]{6,12}$")
        private val VALID_SHIRT_NUMBER_RANGE = 1..100
    }
}
