package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class CreateClubUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(name: String, description: String?, clubLogoUrl: String?, maxMembers: Int?): Result<ClubModel, DataError.Remote> =
        clubRepository.createClub(name = name, description = description, clubLogoUrl = clubLogoUrl, maxMembers = maxMembers)
}
