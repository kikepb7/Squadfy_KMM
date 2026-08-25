package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class AddGuestUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(matchId: String, guestName: String, position: String?, rating: Int?): Result<MatchSignupModel, DataError.Remote> =
        clubRepository.addGuest(matchId = matchId, guestName = guestName, position = position, rating = rating)
}
