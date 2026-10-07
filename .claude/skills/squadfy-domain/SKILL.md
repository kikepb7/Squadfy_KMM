---
name: squadfy-domain
description: |
  Squadfy business domain as implemented by the backend v1: clubs, invitation codes, roles (OWNER/ADMIN/CAPTAIN/PLAYER), hierarchy, kick/ban/transfer, weekly schedule (format, timezone, duration), match announcement (convocatoria) window and waitlist, automatic balanced teams with Elo rating, team balance, match events, minutes, complete/reopen, stats and rankings, push notifications and club mute. Use this skill whenever writing or reviewing logic, tests or UI copy about any of these, or when answering "how should X behave". Trigger on phrases like "convocatoria", "announcement", "apuntarse", "enrollment", "lista de espera", "waitlist", "sorteo", "teams", "team balance", "rating", "Elo", "schedule", "format", "5v5", "timezone", "closesAt", "events", "minutes", "complete", "reopen", "stats", "ranking", "clasificación", "roles", "ban", "veto", "transfer ownership", "mute".
---

# Squadfy domain (client quick reference)

**The backend is the source of truth** (ADR-0005):
- `../Squadfy_Backend/docs/BACKEND.md` §7 (rules), §8 (endpoints), §9–11 (DTOs, enums, errors);
- `../Squadfy_Backend/specs/00N-*/spec.md` (RN-x rules, CA-x criteria).

The app mirror with references is `specs/product/business-rules.md`: Part A = `BE-NNN RN-x`, Part B = client rules `APP-RN-xx`. **If this file disagrees with the backend, the backend wins: fix this file.** Do not invent domain rules in the client.

## App-parity features (BE-008, available in the backend; the app ships them behind flags)
| Feature | Flag | Rules |
|---|---|---|
| **Guests** | `MATCH_GUESTS` | Any member adds guests (name ≤ 80, optional position) while the window is open, **max 2 per member** (409). **Members have priority**: confirmed seats go to members first, then guests by order of addition; a late member pushes the last confirmed guest to the waitlist. Removed by the host or a manager (403 otherwise). Guests draw with rating 1000 and get no rating, stats, events or push. Entries carry `participantType` MEMBER/GUEST, `guestName`, `invitedByMemberId`; `clubMemberId` is null for guests. |
| **Schedule exceptions** | `SCHEDULE_EXCEPTIONS` | One per future match-day date. `CANCELLED` (no match that week) or `RESCHEDULED` + `newScheduledAt` (the match moves, keeps its sign-ups, push `match.rescheduled`). Deleting one undoes it. Managers write; members read. |
| **Absences** | `MEMBER_ABSENCES` | A member's `fromDate`–`toDate` (≤ 1 year, ending today or later): it withdraws them from **open** announcements in that period and mutes the opening and reminder pushes. They may still enroll. Everyone sees them; each member manages their own. |
| **Close and draw times** | `CUSTOM_DRAW_TIME` | `closeDaysBefore`/`closeTime` (default 1 day before at 22:00) and `drawDaysBefore`/`drawTime` (default = close). Close < kickoff and close ≤ draw < kickoff (400). **Teams are published at `drawAt`**, not at close. |
| **Manual score** | `MANUAL_SCORE` | `PUT/DELETE /matches/{id}/score` (0–99) while `SCHEDULED`. If set (`isManualScore`), it is the **official** result for rating and stats; goal events only count for the scorer. |
| Match rating | — | `MatchDto.ratingChanges` = each member's rating delta in a completed match. There is **no manual rating**. |

Removed for good: the manual 1–99 rating, the admin PATCH of a member, the season start and the client "performance index". The per-club photo is in the backend backlog.

## Model
```
User(userId) ─< ClubMember(clubMemberId, role, shirtNumber?, position?) >─ Club(invitationCode, maxMembers?)
Club ── ClubMatchSchedule(dayOfWeek, matchTime, timeZone, format, matchDurationMinutes, isActive)   [one per club]
Club ─< Match(scheduledAt, status, teamA/teamB: [clubMemberId], durationMinutes, minutesPlayed{id→min}, events, scores)
Match ── MatchAnnouncement(opensAt, closesAt, status, maxPlayers, entries[CONFIRMED], waitlist[WAITLISTED])
Club ─< Rating(clubMemberId, rating≈1000 Elo, matchesRated, isProvisional)   Stats derived from COMPLETED matches
```
Teams, entries, ratings and stats reference **`clubMemberId`**, not `userId`. Resolve names and photos with `GET /clubs/{id}/members`. An unknown id means a former member, shown as "Exjugador" (APP-RN-06).

**Enums** (parse safely, with a fallback for unknown values, APP-RN-13):
- `ClubMemberRole` OWNER/ADMIN/CAPTAIN/PLAYER (unknown → PLAYER)
- `PlayerPosition` GOALKEEPER/DEFENDER/MIDFIELDER/FORWARD (unknown → null)
- `MatchFormat` FIVE_A_SIDE=10, SEVEN_A_SIDE=14, ELEVEN_A_SIDE=22 seats
- `MatchStatus` SCHEDULED/COMPLETED/CANCELLED (`IN_PROGRESS` unused)
- `MatchAnnouncementStatus` OPEN/CLOSED/CANCELLED
- `EntryStatus` CONFIRMED/WAITLISTED
- `MyEnrollmentStatus` NOT_ENROLLED/CONFIRMED/WAITLISTED
- `MatchEventType` GOAL/ASSIST/YELLOW_CARD/RED_CARD
- `StatsSortBy` GOALS/ASSISTS/MATCHES/MINUTES/WINS

## Permissions
**Manager = OWNER or ADMIN.** CAPTAIN has **no** management rights. The server enforces everything (403); the client only hides UI (APP-RN-04/05).

| Action | OWNER | ADMIN | CAPTAIN / PLAYER |
|---|---|---|---|
| View club, members, schedule, matches, announcements, ratings, stats | ✅ | ✅ | ✅ |
| Enroll / withdraw self (window open), edit own shirt and position, leave | ✅ (owner cannot leave without transferring) | ✅ | ✅ |
| Edit club, logo, regenerate code, schedule, extra match, cancel, teams AUTO/MANUAL, team balance, events, minutes, complete, reopen | ✅ | ✅ | ❌ |
| Change role, kick, ban, unban | anyone; can assign ADMIN/CAPTAIN/PLAYER | only CAPTAIN/PLAYER targets; toggles only CAPTAIN↔PLAYER | ❌ |
| Transfer ownership | ✅ (becomes ADMIN) | ❌ | ❌ |
| Act on self (kick or change own role) | ❌ (400) | ❌ | ❌ |

## Announcement window (BE-002 RN-4/5/6)
- `opensAt` = start of the day after the last non-cancelled match (club tz), or now.
- `closesAt` = the schedule close time (**default 22:00 club time, the day before the match**; configurable, BE-008). If the match was created after that cut-off, it closes at kickoff. `drawAt` = when teams are published.
- **Client "open" ⇔ `status == OPEN && opensAt ≤ now < closesAt`** (APP-RN-01). `status` may lag up to 5 min after `closesAt`.
- After close, **nobody** can enroll or withdraw (400 `BAD_REQUEST`). Do not offer "ask an admin".
- Full → `WAITLISTED` (FIFO). A confirmed player withdrawing auto-promotes the first waitlisted (push `match.waitlist.promoted`). Leaving the waitlist promotes nobody.
- Only CONFIRMED players play. Cancelling a match cancels its announcement. A cancelled match is never recreated.
- Golden examples (Thursday 20:00, Europe/Madrid, including the DST week) are in `specs/features/005-match-announcement/spec.md`.

## Teams (BE-003)
- They are **published automatically at `drawAt`** (default = close), among CONFIRMED players and guests. Sizes differ by ≤ 1; goalkeepers and then each position are spread; the rating difference is minimized; ties are random.
- Managers may rectify until the match is completed:
  - `AUTO` re-draws;
  - `MANUAL` takes lists of `clubMemberId` that are disjoint, non-empty, differ in size by ≤ 1 and contain only confirmed players.
- `team-balance` (managers only): average and total rating, a per-player rating, and `teamAExpectedScore` from 0 to 1. Returns 409 when there are no teams.
- **The client never computes the draw or the balance.**

## Results (BE-004)
- Managers add or delete events (minute 1–120 optional) only for players on a team and only while `SCHEDULED`.
- **Score** = the manual score if a manager set one (`isManualScore`), otherwise the count of GOAL events per team.
- Minutes default to the match duration; a manager may override them (0–duration) before completing.
- `complete` requires `SCHEDULED`, the kickoff time passed, and teams. It updates the Elo ratings.
- `reopen` only works on the club's **latest** completed match and reverts its ratings.
- Stats (MP, W, D, L, goals, assists, cards, minutes) are derived, never stored as counters. Rankings include every active member, with ties sharing the rank (1, 2, 2, 4). No season filter.

## Push (BE-005)
`data.type`:
- `match.announcement.opened`
- `match.announcement.closing_soon` (24 h before close, only if seats are free and only to non-enrolled members)
- `match.teams.published` (`team` = A or B)
- `match.cancelled`
- `match.rescheduled` (schedule exception moved the match)
- `match.waitlist.promoted` (delivered **even if the club is muted**)
- `new_message`

`data` carries `clubId`, `matchId` and `announcementId`. On receipt, refresh the related screen (ADR-0006).

## Common mistakes
- Using `userId` where the API expects `clubMemberId`.
- Treating CAPTAIN as a manager.
- Trusting `status == OPEN` without checking `closesAt`.
- Showing times in device time instead of `schedule.timeZone`.
- Caching announcements or teams in Room as the source of truth (they are network-first, ADR-0006).
- Branching on `message` instead of `code` for errors.
- Reading `email` or stats from `ClubMemberDto`, which no longer has them.
