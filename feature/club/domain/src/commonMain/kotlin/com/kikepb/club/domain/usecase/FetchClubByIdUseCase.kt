package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult

class FetchClubByIdUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String): EmptyResult<DataError.Remote> =
        clubRepository.fetchClubById(clubId = clubId)
}
