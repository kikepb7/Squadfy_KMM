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
            position = normalizePosition(position)
        )) {
            is Success -> result
            is Failure -> Failure(JoinClubError.Remote(dataError = result.error))
        }
    }

    private fun isValidInvitationCode(code: String): Boolean = code.matches(INVITATION_CODE_REGEX)

    /**
     * The backend only accepts GOALKEEPER/DEFENDER/MIDFIELDER/FORWARD (used to balance the weekly
     * team draw). The join screen still lets players type their position freely, so map common
     * Spanish/English wording to one of those four values and drop anything we can't confidently
     * match - sending an unrecognized value would make the whole join request fail instead of
     * just leaving the position unset for an admin to fix later.
     */
    private fun normalizePosition(raw: String?): String? {
        val normalized = raw?.trim()?.lowercase() ?: return null
        if (normalized.isBlank()) return null

        return when {
            GOALKEEPER_WORDS.any { normalized.contains(it) } -> "GOALKEEPER"
            DEFENDER_WORDS.any { normalized.contains(it) } -> "DEFENDER"
            MIDFIELDER_WORDS.any { normalized.contains(it) } -> "MIDFIELDER"
            FORWARD_WORDS.any { normalized.contains(it) } -> "FORWARD"
            else -> null
        }
    }

    private fun parseShirtNumber(raw: String?): Result<Int?, JoinClubError> {
        if (raw.isNullOrBlank()) return Success(null)
        val n = raw.trim().toIntOrNull()
        return if (n != null && n in VALID_SHIRT_NUMBER_RANGE) Success(n)
        else Failure(JoinClubError.InvalidShirtNumber)
    }

    companion object {
        private val INVITATION_CODE_REGEX = Regex("^[A-Za-z0-9]{6,12}$")
        private val VALID_SHIRT_NUMBER_RANGE = 1..100

        private val GOALKEEPER_WORDS = listOf("portero", "portera", "arquero", "goalkeeper", "keeper")
        private val DEFENDER_WORDS = listOf("defensa", "defensor", "central", "lateral", "defender", "back")
        private val MIDFIELDER_WORDS = listOf("centrocampista", "mediocampista", "medio", "volante", "midfielder", "mediapunta")
        private val FORWARD_WORDS = listOf("delantero", "delantera", "ariete", "atacante", "extremo", "forward", "striker", "winger")
    }
}
