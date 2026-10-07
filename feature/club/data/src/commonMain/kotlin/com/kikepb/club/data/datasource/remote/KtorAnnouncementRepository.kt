package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.AddGuestRequestDTO
import com.kikepb.club.data.dto.CurrentMatchAnnouncementDTO
import com.kikepb.club.data.dto.MatchAnnouncementDTO
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.core.data.networking.apiDelete
import com.kikepb.core.data.networking.apiGet
import com.kikepb.core.data.networking.apiPost
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.mapError
import io.ktor.client.HttpClient

/** Announcements are network-first (ADR-0006): the screen keeps the last value and shows it as stale offline. */
class KtorAnnouncementRepository(private val httpClient: HttpClient) : AnnouncementRepository {

    override suspend fun getCurrent(clubId: String): Result<CurrentAnnouncementModel?, ClubError> =
        when (val result = httpClient.apiGet<CurrentMatchAnnouncementDTO>(route = "/clubs/$clubId/announcements/current")) {
            is Result.Success -> Result.Success(result.data.toDomain())
            // 404 = no scheduled match (AC-005-01)
            is Result.Failure -> if (result.error.status == DataError.Remote.NOT_FOUND) Result.Success(null) else Result.Failure(result.error.toClubError())
        }

    override suspend fun getHistory(clubId: String): Result<List<MatchAnnouncementModel>, ClubError> =
        httpClient.apiGet<List<MatchAnnouncementDTO>>(route = "/clubs/$clubId/announcements").asAnnouncements()

    override suspend fun getByMatch(matchId: String): Result<MatchAnnouncementModel, ClubError> =
        httpClient.apiGet<MatchAnnouncementDTO>(route = "/matches/$matchId/announcement").asAnnouncement()

    override suspend fun enroll(announcementId: String): Result<MatchAnnouncementModel, ClubError> =
        httpClient.apiPost<Unit, MatchAnnouncementDTO>(route = "$ANNOUNCEMENTS/$announcementId/enrollment", body = Unit).asAnnouncement()

    override suspend fun withdraw(announcementId: String): Result<MatchAnnouncementModel, ClubError> =
        httpClient.apiDelete<MatchAnnouncementDTO>(route = "$ANNOUNCEMENTS/$announcementId/enrollment").asAnnouncement()

    override suspend fun addGuest(announcementId: String, name: String, position: PlayerPosition?): Result<MatchAnnouncementModel, ClubError> =
        httpClient.apiPost<AddGuestRequestDTO, MatchAnnouncementDTO>(
            route = "$ANNOUNCEMENTS/$announcementId/guests",
            body = AddGuestRequestDTO(name = name, position = position?.name)
        ).asAnnouncement()

    override suspend fun removeGuest(announcementId: String, guestId: String): Result<MatchAnnouncementModel, ClubError> =
        httpClient.apiDelete<MatchAnnouncementDTO>(route = "$ANNOUNCEMENTS/$announcementId/guests/$guestId").asAnnouncement()

    private fun Result<MatchAnnouncementDTO, RemoteError>.asAnnouncement(): Result<MatchAnnouncementModel, ClubError> =
        mapError { it.toClubError() }.map { it.toDomain() }

    private fun Result<List<MatchAnnouncementDTO>, RemoteError>.asAnnouncements(): Result<List<MatchAnnouncementModel>, ClubError> =
        mapError { it.toClubError() }.map { list -> list.map { it.toDomain() } }

    private companion object {
        const val ANNOUNCEMENTS = "/announcements"
    }
}
