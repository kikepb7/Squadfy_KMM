package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubBanModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

// Thin use cases for the club administration actions of spec 003 (BE-001 RN-7…RN-14).

class RegenerateInvitationCodeUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String): Result<String, ClubError> = clubRepository.regenerateInvitationCode(clubId)
}

class UpdateMyMembershipUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubMemberModel, ClubError> =
        clubRepository.updateMyMembership(clubId = clubId, shirtNumber = shirtNumber, position = position)
}

class LeaveClubUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String): EmptyResult<ClubError> = clubRepository.leaveClub(clubId)
}

class RemoveMemberUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, memberId: String): EmptyResult<ClubError> =
        clubRepository.removeMember(clubId = clubId, memberId = memberId)
}

class ChangeMemberRoleUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, memberId: String, role: ClubMemberRole): Result<ClubMemberModel, ClubError> =
        clubRepository.changeMemberRole(clubId = clubId, memberId = memberId, role = role)
}

class TransferOwnershipUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, memberId: String): Result<ClubModel, ClubError> =
        clubRepository.transferOwnership(clubId = clubId, memberId = memberId)
}

class GetClubBansUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String): Result<List<ClubBanModel>, ClubError> = clubRepository.getBans(clubId)
}

class BanMemberUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, memberId: String): EmptyResult<ClubError> =
        clubRepository.banMember(clubId = clubId, memberId = memberId)
}

class UnbanMemberUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, memberId: String): EmptyResult<ClubError> =
        clubRepository.unbanMember(clubId = clubId, memberId = memberId)
}
