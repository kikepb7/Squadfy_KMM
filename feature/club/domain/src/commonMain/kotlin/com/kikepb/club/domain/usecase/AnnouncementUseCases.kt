package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.core.domain.util.Result

class GetCurrentAnnouncementUseCase(private val repository: AnnouncementRepository) {
    suspend operator fun invoke(clubId: String): Result<CurrentAnnouncementModel?, ClubError> = repository.getCurrent(clubId)
}

class GetAnnouncementHistoryUseCase(private val repository: AnnouncementRepository) {
    suspend operator fun invoke(clubId: String): Result<List<MatchAnnouncementModel>, ClubError> = repository.getHistory(clubId)
}

class EnrollUseCase(private val repository: AnnouncementRepository) {
    suspend operator fun invoke(announcementId: String): Result<MatchAnnouncementModel, ClubError> = repository.enroll(announcementId)
}

class WithdrawUseCase(private val repository: AnnouncementRepository) {
    suspend operator fun invoke(announcementId: String): Result<MatchAnnouncementModel, ClubError> = repository.withdraw(announcementId)
}

/** BE-008 RN-A1: name required (≤ 80), optional position. */
class AddGuestToAnnouncementUseCase(private val repository: AnnouncementRepository) {
    suspend operator fun invoke(announcementId: String, name: String, position: PlayerPosition?): Result<MatchAnnouncementModel, ClubError> =
        repository.addGuest(announcementId = announcementId, name = name.trim(), position = position)

    companion object {
        const val MAX_NAME_LENGTH = 80
    }
}

class RemoveGuestFromAnnouncementUseCase(private val repository: AnnouncementRepository) {
    suspend operator fun invoke(announcementId: String, guestId: String): Result<MatchAnnouncementModel, ClubError> =
        repository.removeGuest(announcementId = announcementId, guestId = guestId)
}

class GetMatchAnnouncementUseCase(private val repository: AnnouncementRepository) {
    suspend operator fun invoke(matchId: String): Result<MatchAnnouncementModel, ClubError> = repository.getByMatch(matchId)
}
