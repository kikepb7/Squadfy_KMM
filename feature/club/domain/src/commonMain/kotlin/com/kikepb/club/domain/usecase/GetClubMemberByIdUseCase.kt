package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

class GetClubMemberByIdUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(memberId: String): Flow<ClubMemberModel?> =
        clubRepository.getClubMemberById(memberId = memberId)
}
