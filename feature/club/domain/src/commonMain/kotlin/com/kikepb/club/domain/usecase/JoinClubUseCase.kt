package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class JoinClubUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(invitationCode: String, shirtNumber: Int?, position: String?): Result<ClubModel, DataError.Remote> =
        clubRepository.joinClub(invitationCode = invitationCode, shirtNumber = shirtNumber, position = position)
}
