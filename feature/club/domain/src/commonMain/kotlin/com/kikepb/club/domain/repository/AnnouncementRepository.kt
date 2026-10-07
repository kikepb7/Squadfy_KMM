package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.core.domain.util.Result

/** Announcements and enrollment (spec 005, BE-002 + BE-008 RN-A). Network-first (ADR-0006). */
interface AnnouncementRepository {
    /** `null` when the club has no scheduled match (404). */
    suspend fun getCurrent(clubId: String): Result<CurrentAnnouncementModel?, ClubError>
    suspend fun getHistory(clubId: String): Result<List<MatchAnnouncementModel>, ClubError>
    suspend fun getByMatch(matchId: String): Result<MatchAnnouncementModel, ClubError>
    suspend fun enroll(announcementId: String): Result<MatchAnnouncementModel, ClubError>
    suspend fun withdraw(announcementId: String): Result<MatchAnnouncementModel, ClubError>
    suspend fun addGuest(announcementId: String, name: String, position: PlayerPosition?): Result<MatchAnnouncementModel, ClubError>
    suspend fun removeGuest(announcementId: String, guestId: String): Result<MatchAnnouncementModel, ClubError>
}
