package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

class GetClubByIdUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(clubId: String): Flow<ClubModel?> =
        clubRepository.getClubById(clubId = clubId)
}