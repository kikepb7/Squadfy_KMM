package com.kikepb.club.datafirebase.repository

import com.kikepb.club.data.dto.ClubMatchScheduleDTO
import com.kikepb.club.data.dto.CurrentMatchAnnouncementDTO
import com.kikepb.club.data.dto.MatchAnnouncementDTO
import com.kikepb.club.data.dto.MatchDTO
import com.kikepb.club.data.dto.MemberAbsenceDTO
import com.kikepb.club.data.dto.PlayerRatingDTO
import com.kikepb.club.data.dto.PlayerStatsDTO
import com.kikepb.club.data.dto.RatingLeaderboardEntryDTO
import com.kikepb.club.data.dto.ScheduleExceptionDTO
import com.kikepb.club.data.dto.TeamBalanceDTO
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.data.mappers.toRequest
import com.kikepb.club.data.mappers.toStatsEntries
import com.kikepb.club.datafirebase.dto.AbsencesRequest
import com.kikepb.club.datafirebase.dto.AddEventRequest
import com.kikepb.club.datafirebase.dto.AddExceptionRequest
import com.kikepb.club.datafirebase.dto.AddGuestRequest
import com.kikepb.club.datafirebase.dto.AnnouncementIdRequest
import com.kikepb.club.datafirebase.dto.ClubIdRequest
import com.kikepb.club.datafirebase.dto.ClubMatchesRequest
import com.kikepb.club.datafirebase.dto.CreateAbsenceRequest
import com.kikepb.club.datafirebase.dto.CreateExtraMatchRequest
import com.kikepb.club.datafirebase.dto.DeleteAbsenceRequest
import com.kikepb.club.datafirebase.dto.DeleteEventRequest
import com.kikepb.club.datafirebase.dto.DeleteExceptionRequest
import com.kikepb.club.datafirebase.dto.GenerateTeamsRequest
import com.kikepb.club.datafirebase.dto.MatchIdRequest
import com.kikepb.club.datafirebase.dto.NotificationSettingsDto
import com.kikepb.club.datafirebase.dto.RemoveGuestRequest
import com.kikepb.club.datafirebase.dto.ScheduleRequest
import com.kikepb.club.datafirebase.dto.SetMinutesRequest
import com.kikepb.club.datafirebase.dto.SetMutedRequest
import com.kikepb.club.datafirebase.dto.SetScoreRequest
import com.kikepb.club.datafirebase.dto.SeasonDto
import com.kikepb.club.datafirebase.dto.SeasonRequest
import com.kikepb.club.datafirebase.dto.StatsRequest
import com.kikepb.club.datafirebase.dto.UpdateSeasonRequest
import com.kikepb.club.domain.model.SeasonModel
import com.kikepb.club.domain.model.SeasonStatus
import com.kikepb.club.domain.repository.SeasonRepository
import com.kikepb.club.datafirebase.util.asClubResult
import com.kikepb.club.datafirebase.util.asEmptyClubResult
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.repository.AbsenceRepository
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.club.domain.repository.NotificationSettingsRepository
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.repository.StandingsRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.firebase.functions.FunctionsClient
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * The weekly cycle on Firebase (spec 019 AC-019-08…13). The Cloud Functions answer the backend's JSON (BACKEND.md §9),
 * so these repositories reuse its DTOs and mappers (`feature/club/data-contract`) and behave as the Ktor ones:
 * network-first (ADR-0006), refreshed by the live notices of [FirebaseClubLiveUpdates].
 */

class FirebaseScheduleRepository(private val functions: FunctionsClient) : ScheduleRepository {

    private val lastKnown = mutableMapOf<String, ClubScheduleModel?>()

    override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> =
        when (val result = functions.call<ClubIdRequest, ClubMatchScheduleDTO>("getSchedule", ClubIdRequest(clubId))) {
            is Result.Success -> Result.Success(result.data.toDomain().also { lastKnown[clubId] = it })
            is Result.Failure -> when {
                result.error.status == DataError.Remote.NOT_FOUND -> Result.Success(null).also { lastKnown[clubId] = null }
                clubId in lastKnown && result.error.status == DataError.Remote.NO_INTERNET -> Result.Success(lastKnown[clubId])
                else -> Result.Failure(result.error.toClubError())
            }
        }

    override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError> =
        save("createSchedule", clubId, draft, includeDeadlines)

    override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError> =
        save("updateSchedule", clubId, draft, includeDeadlines)

    private suspend fun save(name: String, clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError> {
        val body = draft.toRequest(includeDeadlines)
        val request = ScheduleRequest(
            clubId = clubId, matchDayOfWeek = body.matchDayOfWeek, matchTime = body.matchTime, timeZone = body.timeZone,
            format = body.format, matchDurationMinutes = body.matchDurationMinutes, isActive = body.isActive,
            closeDaysBefore = body.closeDaysBefore, closeTime = body.closeTime, drawDaysBefore = body.drawDaysBefore, drawTime = body.drawTime
        )
        return functions.call<ScheduleRequest, ClubMatchScheduleDTO>(name, request)
            .asClubResult()
            .map { dto -> dto.toDomain().also { lastKnown[clubId] = it } }
    }

    override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> =
        functions.call<ClubIdRequest, List<ScheduleExceptionDTO>>("getScheduleExceptions", ClubIdRequest(clubId))
            .asClubResult()
            .map { exceptions -> exceptions.map { it.toDomain() }.sortedBy { it.date } }

    override suspend fun addException(
        clubId: String,
        date: LocalDate,
        type: ScheduleExceptionType,
        newScheduledAt: Instant?,
        reason: String?
    ): Result<ScheduleExceptionModel, ClubError> =
        functions.call<AddExceptionRequest, ScheduleExceptionDTO>(
            "addScheduleException",
            AddExceptionRequest(clubId, date.toString(), type.name, newScheduledAt?.toString(), reason)
        ).asClubResult().map { it.toDomain() }

    override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> =
        functions.run("deleteScheduleException", DeleteExceptionRequest(clubId, exceptionId)).asEmptyClubResult()
}

class FirebaseAnnouncementRepository(private val functions: FunctionsClient) : AnnouncementRepository {

    override suspend fun getCurrent(clubId: String): Result<CurrentAnnouncementModel?, ClubError> =
        when (val result = functions.call<ClubIdRequest, CurrentMatchAnnouncementDTO>("getCurrentAnnouncement", ClubIdRequest(clubId))) {
            is Result.Success -> Result.Success(result.data.toDomain())
            // 404 = no scheduled match (AC-005-01)
            is Result.Failure -> if (result.error.status == DataError.Remote.NOT_FOUND) Result.Success(null) else Result.Failure(result.error.toClubError())
        }

    override suspend fun getHistory(clubId: String): Result<List<MatchAnnouncementModel>, ClubError> =
        functions.call<ClubIdRequest, List<MatchAnnouncementDTO>>("getAnnouncementHistory", ClubIdRequest(clubId))
            .asClubResult().map { list -> list.map { it.toDomain() } }

    override suspend fun getByMatch(matchId: String): Result<MatchAnnouncementModel, ClubError> =
        announcement("getAnnouncementByMatch", MatchIdRequest(matchId))

    override suspend fun enroll(announcementId: String): Result<MatchAnnouncementModel, ClubError> =
        announcement("enroll", AnnouncementIdRequest(announcementId))

    override suspend fun withdraw(announcementId: String): Result<MatchAnnouncementModel, ClubError> =
        announcement("withdraw", AnnouncementIdRequest(announcementId))

    override suspend fun addGuest(announcementId: String, name: String, position: PlayerPosition?): Result<MatchAnnouncementModel, ClubError> =
        announcement("addGuest", AddGuestRequest(announcementId, name, position?.name))

    override suspend fun removeGuest(announcementId: String, guestId: String): Result<MatchAnnouncementModel, ClubError> =
        announcement("removeGuest", RemoveGuestRequest(announcementId, guestId))

    private suspend inline fun <reified I> announcement(name: String, input: I): Result<MatchAnnouncementModel, ClubError> =
        functions.call<I, MatchAnnouncementDTO>(name, input).asClubResult().map { it.toDomain() }
}

class FirebaseMatchRepository(private val functions: FunctionsClient) : MatchRepository {

    override suspend fun getMatch(matchId: String): Result<MatchModel, ClubError> = match("getMatch", MatchIdRequest(matchId))

    override suspend fun getClubMatches(clubId: String, status: MatchStatus?): Result<List<MatchModel>, ClubError> =
        functions.call<ClubMatchesRequest, List<MatchDTO>>("getClubMatches", ClubMatchesRequest(clubId, status?.name))
            .asClubResult().map { list -> list.map { it.toDomain() } }

    override suspend fun createExtraMatch(clubId: String, scheduledAt: Instant, format: MatchFormat?, durationMinutes: Int?): Result<MatchModel, ClubError> =
        match("createExtraMatch", CreateExtraMatchRequest(clubId, scheduledAt.toString(), format?.name, durationMinutes))

    override suspend fun cancel(matchId: String): Result<MatchModel, ClubError> = match("cancelMatch", MatchIdRequest(matchId))

    override suspend fun complete(matchId: String): Result<MatchModel, ClubError> = match("completeMatch", MatchIdRequest(matchId))

    override suspend fun reopen(matchId: String): Result<MatchModel, ClubError> = match("reopenMatch", MatchIdRequest(matchId))

    override suspend fun generateTeams(matchId: String, teamA: List<String>?, teamB: List<String>?): Result<MatchModel, ClubError> {
        val manual = teamA != null && teamB != null
        return match(
            "generateTeams",
            GenerateTeamsRequest(matchId, if (manual) "MANUAL" else "AUTO", teamA.takeIf { manual }, teamB.takeIf { manual })
        )
    }

    override suspend fun getTeamBalance(matchId: String): Result<TeamBalanceModel?, ClubError> =
        when (val result = functions.call<MatchIdRequest, TeamBalanceDTO>("getTeamBalance", MatchIdRequest(matchId))) {
            is Result.Success -> Result.Success(result.data.toDomain())
            // 409 = no teams yet: the panel is hidden (AC-006-03)
            is Result.Failure -> if (result.error.status == DataError.Remote.CONFLICT) Result.Success(null) else Result.Failure(result.error.toClubError())
        }

    override suspend fun addEvent(matchId: String, clubMemberId: String, type: MatchEventType, minute: Int?): Result<MatchModel, ClubError> =
        match("addMatchEvent", AddEventRequest(matchId, clubMemberId, type.name, minute))

    override suspend fun deleteEvent(matchId: String, eventId: String): Result<MatchModel, ClubError> =
        match("deleteMatchEvent", DeleteEventRequest(matchId, eventId))

    override suspend fun setMinutes(matchId: String, clubMemberId: String, minutes: Int): Result<MatchModel, ClubError> =
        match("setPlayerMinutes", SetMinutesRequest(matchId, clubMemberId, minutes))

    override suspend fun setManualScore(matchId: String, teamAScore: Int, teamBScore: Int): Result<MatchModel, ClubError> =
        match("setMatchScore", SetScoreRequest(matchId, teamAScore, teamBScore))

    override suspend fun clearManualScore(matchId: String): Result<MatchModel, ClubError> =
        match("clearMatchScore", MatchIdRequest(matchId))

    private suspend inline fun <reified I> match(name: String, input: I): Result<MatchModel, ClubError> =
        functions.call<I, MatchDTO>(name, input).asClubResult().map { it.toDomain() }
}

class FirebaseStandingsRepository(private val functions: FunctionsClient) : StandingsRepository {

    override suspend fun getRatings(clubId: String, seasonId: String?): Result<List<RatingEntry>, ClubError> =
        functions.call<SeasonRequest, List<RatingLeaderboardEntryDTO>>("getRatings", SeasonRequest(clubId, seasonId))
            .asClubResult().map { list -> list.map { it.toDomain() } }

    override suspend fun getMyRating(clubId: String, seasonId: String?): Result<MyRating, ClubError> =
        functions.call<SeasonRequest, PlayerRatingDTO>("getMyRating", SeasonRequest(clubId, seasonId)).asClubResult().map { it.toDomain() }

    override suspend fun getStats(
        clubId: String,
        sortBy: StatsSortBy,
        period: ClosedRange<LocalDate>?,
        seasonId: String?
    ): Result<List<StatsEntry>, ClubError> =
        functions.call<StatsRequest, List<PlayerStatsDTO>>(
            "getStats",
            StatsRequest(clubId, sortBy.name, period?.start?.toString(), period?.endInclusive?.toString(), seasonId)
        ).asClubResult().map { it.toStatsEntries() }

    override suspend fun getMyStats(clubId: String): Result<PlayerStats, ClubError> =
        functions.call<ClubIdRequest, PlayerStatsDTO>("getMyStats", ClubIdRequest(clubId)).asClubResult().map { it.toDomain() }
}

class FirebaseAbsenceRepository(private val functions: FunctionsClient) : AbsenceRepository {

    override suspend fun getAbsences(clubId: String, from: LocalDate?, to: LocalDate?): Result<List<MemberAbsenceModel>, ClubError> =
        functions.call<AbsencesRequest, List<MemberAbsenceDTO>>("getAbsences", AbsencesRequest(clubId, from?.toString(), to?.toString()))
            .asClubResult().map { list -> list.map { it.toDomain() } }

    override suspend fun addMyAbsence(clubId: String, fromDate: LocalDate, toDate: LocalDate, reason: String?): Result<MemberAbsenceModel, ClubError> =
        functions.call<CreateAbsenceRequest, MemberAbsenceDTO>(
            "createAbsence", CreateAbsenceRequest(clubId, fromDate.toString(), toDate.toString(), reason)
        ).asClubResult().map { it.toDomain() }

    override suspend fun deleteMyAbsence(clubId: String, absenceId: String): EmptyResult<ClubError> =
        functions.run("deleteAbsence", DeleteAbsenceRequest(clubId, absenceId)).asEmptyClubResult()
}

class FirebaseNotificationSettingsRepository(private val functions: FunctionsClient) : NotificationSettingsRepository {

    override suspend fun isMuted(clubId: String): Result<Boolean, ClubError> =
        functions.call<ClubIdRequest, NotificationSettingsDto>("getClubNotificationSettings", ClubIdRequest(clubId))
            .asClubResult().map { it.muted }

    override suspend fun setMuted(clubId: String, muted: Boolean): Result<Boolean, ClubError> =
        functions.call<SetMutedRequest, NotificationSettingsDto>("setClubMuted", SetMutedRequest(clubId, muted))
            .asClubResult().map { it.muted }
}

/** Seasons of the club (spec 020): `getSeasons` and `updateSeason`. */
class FirebaseSeasonRepository(private val functions: FunctionsClient) : SeasonRepository {

    override suspend fun getSeasons(clubId: String): Result<List<SeasonModel>, ClubError> =
        functions.call<ClubIdRequest, List<SeasonDto>>("getSeasons", ClubIdRequest(clubId))
            .asClubResult().map { list -> list.mapNotNull { it.toDomain() } }

    override suspend fun updateSeason(clubId: String, seasonId: String, startsOn: LocalDate, endsOn: LocalDate): Result<SeasonModel, ClubError> =
        functions.call<UpdateSeasonRequest, SeasonDto>("updateSeason", UpdateSeasonRequest(clubId, seasonId, startsOn.toString(), endsOn.toString()))
            .asClubResult().map { it.toDomain() ?: throw IllegalStateException("Invalid season") }
}

/** A season with unreadable dates is skipped; an unknown status counts as closed (read-only). */
internal fun SeasonDto.toDomain(): SeasonModel? {
    val start = runCatching { LocalDate.parse(startsOn) }.getOrNull() ?: return null
    val end = runCatching { LocalDate.parse(endsOn) }.getOrNull() ?: return null
    return SeasonModel(
        id = id,
        name = name,
        startsOn = start,
        endsOn = end,
        status = SeasonStatus.entries.firstOrNull { it.name == status } ?: SeasonStatus.CLOSED
    )
}
