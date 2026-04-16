package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result.Failure
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SyncClubDetailUseCase(private val clubRepository: ClubRepository) {

    suspend operator fun invoke(clubId: String): EmptyResult<DataError.Remote> = coroutineScope {
        val clubDeferred = async { clubRepository.fetchClubById(clubId) }
        val membersDeferred = async { clubRepository.fetchClubMembers(clubId) }

        val clubResult = clubDeferred.await()
        val membersResult = membersDeferred.await()

        clubResult as? Failure ?: membersResult
    }
}
