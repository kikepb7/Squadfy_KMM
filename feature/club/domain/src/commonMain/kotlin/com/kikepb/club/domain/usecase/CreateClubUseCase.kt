package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository

class CreateClubUseCase(
    private val clubRepository: ClubRepository
) {
    suspend fun createClub(
        name: String,
        description: String?,
        clubLogoUrl: String?,
        maxMembers: Int?
    ) = clubRepository.createClub(
        name = name,
        description = description,
        clubLogoUrl = clubLogoUrl,
        maxMembers = maxMembers
    )
}
