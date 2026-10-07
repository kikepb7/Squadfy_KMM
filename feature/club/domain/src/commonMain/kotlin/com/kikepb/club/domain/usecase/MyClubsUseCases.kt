package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MyClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.EmptyResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveMyClubsUseCase(
    private val clubRepository: ClubRepository,
    private val sessionStorage: SessionStorage
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<MyClubModel>> =
        sessionStorage.observeAuthInfo().flatMapLatest { authInfo ->
            val userId = authInfo?.user?.id ?: return@flatMapLatest flowOf(emptyList())
            combine(clubRepository.observeMyClubs(), clubRepository.observeMembershipsOfUser(userId)) { clubs, memberships ->
                val roleByClub = memberships.associate { it.clubId to it.role }
                clubs.map { club -> MyClubModel(club = club, myRole = roleByClub[club.id]) }
            }
        }
}

class FetchMyClubsUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(): EmptyResult<ClubError> = clubRepository.fetchMyClubs()
}
