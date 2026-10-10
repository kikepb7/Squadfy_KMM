package com.kikepb.club.presentation.preview

import androidx.compose.foundation.text.input.TextFieldState
import com.kikepb.club.domain.model.AnnouncementEntry
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.ClubBanModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.EntryStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MatchEventModel
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchRatingChange
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.model.MyClubModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.ParticipantType
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.model.TeamStrengthModel
import com.kikepb.club.presentation.absences.AbsencesState
import com.kikepb.club.presentation.announcement.AnnouncementState
import com.kikepb.club.presentation.bans.ClubBansState
import com.kikepb.club.presentation.clubs.ClubsListState
import com.kikepb.club.presentation.detail.ClubDetailState
import com.kikepb.club.presentation.match.MatchDetailState
import com.kikepb.club.presentation.memberdetail.MemberDetailState
import com.kikepb.club.presentation.memberdetail.MemberStatsState
import com.kikepb.club.presentation.schedule.ScheduleState
import com.kikepb.club.presentation.settings.ClubSettingsState
import com.kikepb.club.presentation.standings.StandingsState
import com.kikepb.club.presentation.teams.TeamsState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

/**
 * Sample data of the mockups (spec 018): Albaicín FC, a 7-a-side club of 14, with Álvaro (me, owner) in team A.
 * Only for the previews.
 */
internal object ClubSamples {
    const val ZONE = "Europe/Madrid"
    const val ME = "m-alvaro"
    val now: Instant = Instant.parse("2026-10-13T16:45:00Z")
    private val matchAt = Instant.parse("2026-10-15T18:00:00Z")

    val club = ClubModel(
        id = "albaicin", name = "Albaicín FC", description = "Fútbol 7 los jueves en el Albaicín", clubLogoUrl = null,
        ownerId = "u-alvaro", invitationCode = "K7Q2M9AB", maxMembers = 20, membersCount = 14
    )

    private fun member(id: String, name: String, number: Int, position: PlayerPosition, role: ClubMemberRole = ClubMemberRole.PLAYER) =
        ClubMemberModel(
            id = id, clubId = club.id, userId = "u-${id.removePrefix("m-")}", username = name, profilePictureUrl = null,
            shirtNumber = number, position = position, role = role
        )

    val alvaro = member(ME, "Álvaro Gil", 10, PlayerPosition.MIDFIELDER, ClubMemberRole.OWNER)
    val teamAMembers = listOf(
        member("m-marcos", "Marcos Peña", 1, PlayerPosition.GOALKEEPER),
        member("m-ivan", "Iván Soto", 4, PlayerPosition.DEFENDER),
        member("m-sergio", "Sergio Mora", 5, PlayerPosition.DEFENDER),
        member("m-hugo", "Hugo Prieto", 3, PlayerPosition.DEFENDER),
        member("m-dani", "Dani Moreno", 8, PlayerPosition.MIDFIELDER, ClubMemberRole.CAPTAIN),
        alvaro,
        member("m-alex", "Álex Ruiz", 9, PlayerPosition.FORWARD)
    )
    val teamBMembers = listOf(
        member("m-nico", "Nico Vidal", 13, PlayerPosition.GOALKEEPER),
        member("m-jaime", "Jaime Ortiz", 2, PlayerPosition.DEFENDER),
        member("m-tomas", "Tomás Gallego", 6, PlayerPosition.DEFENDER),
        member("m-gabi", "Gabi Sanz", 15, PlayerPosition.DEFENDER),
        member("m-pablo", "Pablo Ruiz", 16, PlayerPosition.MIDFIELDER),
        member("m-nacho", "Nacho León", 14, PlayerPosition.MIDFIELDER),
        member("m-leo", "Leo Vega", 11, PlayerPosition.FORWARD, ClubMemberRole.ADMIN)
    )
    val members = teamAMembers + teamBMembers
    val membersById = members.associateBy { it.id }

    private fun entry(memberId: String, status: EntryStatus = EntryStatus.CONFIRMED) = AnnouncementEntry(
        id = "e-$memberId", participantType = ParticipantType.MEMBER, clubMemberId = memberId, guestName = null,
        guestPosition = null, invitedByMemberId = null, status = status, enrolledAt = Instant.parse("2026-10-10T09:00:00Z")
    )

    private fun announcement(confirmedIds: List<String>, waitlistIds: List<String> = emptyList()) = MatchAnnouncementModel(
        id = "a-1", matchId = "match-1", clubId = club.id, maxPlayers = 14, confirmedCount = confirmedIds.size,
        waitlistCount = waitlistIds.size, opensAt = Instant.parse("2026-10-09T22:00:00Z"),
        closesAt = Instant.parse("2026-10-15T20:00:00Z").minusDays(1), drawAt = Instant.parse("2026-10-15T10:00:00Z"),
        status = AnnouncementStatus.OPEN, entries = confirmedIds.map { entry(it) },
        waitlist = waitlistIds.map { entry(it, EntryStatus.WAITLISTED) }
    )

    private fun Instant.minusDays(days: Int) = Instant.fromEpochMilliseconds(toEpochMilliseconds() - days * 86_400_000L)

    private val nineConfirmed = members.filter { it.id != ME }.take(9).map { it.id }

    /** Partido A: 9 of 14, sign-up open, I am not in yet. */
    fun announcement(mine: MyEnrollmentStatus = MyEnrollmentStatus.NOT_ENROLLED, now: Instant = this.now): AnnouncementState {
        val confirmed = if (mine == MyEnrollmentStatus.CONFIRMED) nineConfirmed.take(8) + ME else nineConfirmed
        val waitlist = if (mine == MyEnrollmentStatus.WAITLISTED) listOf("m-leo", ME) else emptyList()
        return AnnouncementState(
            now = now,
            hasLoaded = true,
            current = CurrentAnnouncementModel(
                announcement = announcement(confirmed, waitlist),
                matchScheduledAt = matchAt,
                myStatus = mine,
                myWaitlistPosition = if (mine == MyEnrollmentStatus.WAITLISTED) 2 else null
            ),
            timeZoneId = ZONE,
            members = membersById,
            me = alvaro
        )
    }

    /** After the close: sign-up closed. */
    val announcementClosed: AnnouncementState get() = announcement(now = Instant.parse("2026-10-14T21:00:00Z"))

    private val events = listOf(
        MatchEventModel("ev-1", "m-alex", MatchEventType.GOAL, 12, Instant.parse("2026-10-15T18:12:00Z")),
        MatchEventModel("ev-2", "m-dani", MatchEventType.ASSIST, 12, Instant.parse("2026-10-15T18:12:10Z")),
        MatchEventModel("ev-3", "m-leo", MatchEventType.GOAL, 27, Instant.parse("2026-10-15T18:27:00Z")),
        MatchEventModel("ev-4", "m-hugo", MatchEventType.YELLOW_CARD, 41, Instant.parse("2026-10-15T18:41:00Z")),
        MatchEventModel("ev-5", ME, MatchEventType.GOAL, 52, Instant.parse("2026-10-15T18:52:00Z"))
    )

    fun match(status: MatchStatus) = MatchModel(
        id = "match-1", clubId = club.id, scheduledAt = matchAt, status = status,
        enrolledPlayers = members.map { it.id }, enrolledGuests = emptyList(),
        teamA = teamAMembers.map { it.id }, teamB = teamBMembers.map { it.id }, teamAGuests = emptyList(), teamBGuests = emptyList(),
        durationMinutes = 60, minutesPlayed = emptyMap(),
        teamAScore = if (status == MatchStatus.SCHEDULED) 0 else 2, teamBScore = if (status == MatchStatus.SCHEDULED) 0 else 1,
        isManualScore = false, events = if (status == MatchStatus.SCHEDULED) emptyList() else events,
        ratingChanges = if (status == MatchStatus.COMPLETED) mapOf(
            ME to 12, "m-alex" to 8, "m-dani" to 6, "m-marcos" to 4, "m-ivan" to 5, "m-sergio" to 3, "m-hugo" to 2,
            "m-leo" to -3, "m-nico" to -5, "m-pablo" to -4, "m-nacho" to -6, "m-jaime" to -2, "m-tomas" to -4, "m-gabi" to -3
        ) else emptyMap(),
        scheduleDate = LocalDate(2026, 10, 15)
    )

    val balance = TeamBalanceModel(
        teamA = TeamStrengthModel(players = 7, averageRating = 1032, totalRating = 7224, playerRatings = emptyList()),
        teamB = TeamStrengthModel(players = 7, averageRating = 1027, totalRating = 7189, playerRatings = emptyList()),
        averageRatingDifference = 5,
        teamAExpectedScore = 0.51
    )

    fun matchDetail(status: MatchStatus, now: Instant = Instant.parse("2026-10-15T19:30:00Z")) = MatchDetailState(
        hasLoaded = true, match = match(status), balance = balance, members = membersById, myMemberId = ME,
        isManager = true, timeZoneId = ZONE, format = MatchFormat.SEVEN_A_SIDE, now = now
    )

    val teams = TeamsState(
        hasLoaded = true, hasMatch = true, match = match(MatchStatus.SCHEDULED), drawAt = Instant.parse("2026-10-15T10:00:00Z"),
        balance = balance, members = membersById, myMemberId = ME, timeZoneId = ZONE
    )

    private val ratings = listOf(
        "m-alex" to 1088, ME to 1061, "m-dani" to 1049, "m-leo" to 1033, "m-marcos" to 1027, "m-sergio" to 1020,
        "m-nico" to 1012, "m-ivan" to 1008, "m-pablo" to 1001, "m-hugo" to 996
    ).mapIndexed { index, (id, rating) -> RatingEntry(rank = index + 1, clubMemberId = id, rating = rating, matchesRated = 12 + index, isProvisional = index == 9) }

    val standings = StandingsState(
        hasLoaded = true, ratings = ratings, members = membersById, myMemberId = ME,
        lastMatchDeltas = mapOf("m-alex" to 8, ME to 12, "m-dani" to -3, "m-leo" to 5, "m-marcos" to 0, "m-sergio" to -6),
        stats = ratings.map { StatsEntry(it.rank, stats(it.clubMemberId, goals = 20 - it.rank * 2)) }
    )

    private fun stats(id: String, goals: Int) = PlayerStats(
        clubMemberId = id, matchesPlayed = 24, wins = 15, draws = 4, losses = 5, goals = goals, assists = 9,
        yellowCards = 2, redCards = 0, minutesPlayed = 1320
    )

    val memberDetail = MemberDetailState(isLoading = false, member = alvaro, me = alvaro)
    val memberStats = MemberStatsState(
        stats = stats(ME, goals = 7), rating = 1061, rank = 2, totalPlayers = 14,
        recentChanges = listOf(12, -3, 8, 5, -2, 6).mapIndexed { i, change ->
            MatchRatingChange(matchId = "m$i", scheduledAt = Instant.fromEpochMilliseconds(matchAt.toEpochMilliseconds() - i * 7 * 86_400_000L), change = change)
        }
    )

    val clubDetail = ClubDetailState(isLoading = false, club = club, members = members, myMembership = alvaro)
    val clubSettings = ClubSettingsState(muted = false, absencesEnabled = true)
    val clubsList = ClubsListState(
        clubs = listOf(
            MyClubModel(club, ClubMemberRole.OWNER),
            MyClubModel(club.copy(id = "cartuja", name = "Cartuja 7", membersCount = 12), ClubMemberRole.PLAYER)
        ),
        isLoading = false,
        hasFetched = true
    )

    private val schedule = ClubScheduleModel(
        id = "s-1", clubId = club.id, dayOfWeek = DayOfWeek.THURSDAY, matchTime = LocalTime(20, 0), timeZone = ZONE,
        format = MatchFormat.SEVEN_A_SIDE, maxPlayers = 14, durationMinutes = 60, closeDaysBefore = 1, closeTime = LocalTime(22, 0),
        drawDaysBefore = 0, drawTime = LocalTime(12, 0), isActive = true
    )
    fun schedule() = ScheduleState(
        isLoading = false, canEdit = true, customDeadlinesEnabled = true, schedule = schedule, form = ScheduleDraft.from(schedule),
        timeZone = TextFieldState(ZONE), duration = TextFieldState("60")
    )

    val absences = AbsencesState(
        isLoading = false,
        absences = listOf(
            MemberAbsenceModel("ab-1", ME, LocalDate(2026, 10, 22), LocalDate(2026, 11, 5), "Vacaciones"),
            MemberAbsenceModel("ab-2", "m-marcos", LocalDate(2026, 10, 12), LocalDate(2026, 10, 19), null),
            MemberAbsenceModel("ab-3", "m-ivan", LocalDate(2026, 11, 1), LocalDate(2026, 11, 15), "Lesión"),
            MemberAbsenceModel("ab-4", "m-sergio", LocalDate(2026, 10, 24), LocalDate(2026, 10, 24), null)
        ),
        timeZoneId = ZONE,
        members = membersById,
        myMemberId = ME
    )

    val bans = ClubBansState(
        bans = listOf(ClubBanModel("m-x", "u-x", "Rubén Castro", "2026-09-30T10:00:00Z"), ClubBanModel("m-y", "u-y", "Kike Lara", "2026-08-02T10:00:00Z")),
        isLoading = false
    )
}
