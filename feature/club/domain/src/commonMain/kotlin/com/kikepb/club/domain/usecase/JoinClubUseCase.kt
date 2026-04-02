package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository

class JoinClubUseCase(
    private val clubRepository: ClubRepository
) {
    suspend fun joinClub(
        invitationCode: String,
        shirtNumber: Int?,
        position: String?
    ) = clubRepository.joinClub(
        invitationCode = invitationCode,
        shirtNumber = shirtNumber,
        position = position
    )
}
