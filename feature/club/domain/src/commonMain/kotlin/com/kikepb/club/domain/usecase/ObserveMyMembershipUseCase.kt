package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The current user's membership in a club: the member whose `userId` is the session user (APP-RN-04). */
class ObserveMyMembershipUseCase(
    private val clubRepository: ClubRepository,
    private val sessionStorage: SessionStorage
) {
    operator fun invoke(clubId: String): Flow<ClubMemberModel?> =
        combine(clubRepository.getClubMembers(clubId = clubId), sessionStorage.observeAuthInfo()) { members, authInfo ->
            members.firstOrNull { it.userId == authInfo?.user?.id }
        }
}
