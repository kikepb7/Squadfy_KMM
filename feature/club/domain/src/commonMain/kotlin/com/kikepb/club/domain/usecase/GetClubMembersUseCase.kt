package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository

class GetClubMembersUseCase(
    private val clubRepository: ClubRepository
) {
    suspend fun getClubMembers(clubId: String) = clubRepository.getClubMembers(clubId = clubId)
}
