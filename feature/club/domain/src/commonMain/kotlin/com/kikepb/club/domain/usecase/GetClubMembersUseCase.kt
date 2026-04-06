package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

class GetClubMembersUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(clubId: String): Flow<List<ClubMemberModel>> =
        clubRepository.getClubMembers(clubId = clubId)
}
