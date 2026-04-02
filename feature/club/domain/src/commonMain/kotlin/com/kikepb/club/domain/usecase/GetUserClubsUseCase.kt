package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository

class GetUserClubsUseCase(
    private val clubRepository: ClubRepository
) {
    suspend fun getUserClubs() = clubRepository.getUserClubs()
}
