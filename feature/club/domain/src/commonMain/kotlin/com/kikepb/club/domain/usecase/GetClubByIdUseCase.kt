package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository

class GetClubByIdUseCase(private val clubRepository: ClubRepository
) {
    suspend fun getClubById(clubId: String) = clubRepository.getClubById(clubId = clubId)
}