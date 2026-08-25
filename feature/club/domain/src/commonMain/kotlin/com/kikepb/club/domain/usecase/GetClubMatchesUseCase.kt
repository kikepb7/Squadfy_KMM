package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class GetClubMatchesUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String): Result<List<ClubMatchModel>, DataError.Remote> =
        clubRepository.getMatchesForClub(clubId = clubId)
}
