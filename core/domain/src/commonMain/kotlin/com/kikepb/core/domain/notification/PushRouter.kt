package com.kikepb.core.domain.notification

import com.kikepb.core.domain.update.AppUpdatePolicy
import com.kikepb.core.domain.update.AppVersionConfig
import com.kikepb.core.domain.update.UpdateStatus

/** A push as the app receives it: `data` (all strings, BACKEND.md §13) plus the visible text. */
data class PushMessage(
    val data: Map<String, String>,
    val title: String? = null,
    val body: String? = null
) {
    val type: String? get() = data[KEY_TYPE]
    val clubId: String? get() = data[KEY_CLUB_ID]
    val matchId: String? get() = data[KEY_MATCH_ID]

    companion object {
        const val KEY_TYPE = "type"
        const val KEY_CLUB_ID = "clubId"
        const val KEY_MATCH_ID = "matchId"
        const val KEY_CHAT_ID = "chatId"
        const val KEY_URL = "url"
    }
}

/** Where tapping a push takes the user (AC-009-03). */
sealed interface PushDestination {
    data class ClubAnnouncement(val clubId: String) : PushDestination
    data class Match(val matchId: String, val clubId: String) : PushDestination
    /** Spec 018: «teams published» opens the draw result («Juegas en el Equipo A»). */
    data class TeamsDrawn(val matchId: String, val clubId: String) : PushDestination
    data class Chat(val chatId: String) : PushDestination
    /** Spec 021 APP-RN-P7: a new version or an announcement of the app opens the store or a web page. */
    data class External(val url: String) : PushDestination
    /** Unknown type or missing ids: just open the app. */
    data object None : PushDestination
}

object PushRouter {

    const val ANNOUNCEMENT_OPENED = "match.announcement.opened"
    const val ANNOUNCEMENT_CLOSING_SOON = "match.announcement.closing_soon"
    const val WAITLIST_PROMOTED = "match.waitlist.promoted"
    const val TEAMS_PUBLISHED = "match.teams.published"
    const val MATCH_CANCELLED = "match.cancelled"
    const val MATCH_RESCHEDULED = "match.rescheduled"
    const val NEW_MESSAGE = "new_message"

    // Spec 021
    const val MATCH_REMINDER = "match.reminder"
    const val MATCH_COMPLETED = "match.completed"
    const val CLUB_MEMBER_JOINED = "club.member_joined"
    const val CLUB_ROLE_CHANGED = "club.role_changed"
    const val CLUB_OWNERSHIP_TRANSFERRED = "club.ownership_transferred"
    const val SEASON_STARTED = "season.started"
    const val SEASON_CLOSED = "season.closed"
    const val APP_UPDATE = "app.update"
    const val APP_ANNOUNCEMENT = "app.announcement"

    private val clubAnnouncementTypes = setOf(
        ANNOUNCEMENT_OPENED, ANNOUNCEMENT_CLOSING_SOON, WAITLIST_PROMOTED, MATCH_CANCELLED, MATCH_RESCHEDULED, MATCH_REMINDER
    )
    private val clubTypes = setOf(CLUB_MEMBER_JOINED, CLUB_ROLE_CHANGED, CLUB_OWNERSHIP_TRANSFERRED, SEASON_STARTED, SEASON_CLOSED)
    private val appTypes = setOf(APP_UPDATE, APP_ANNOUNCEMENT)

    fun route(data: Map<String, String>): PushDestination {
        val type = data[PushMessage.KEY_TYPE]
        val clubId = data[PushMessage.KEY_CLUB_ID]
        val matchId = data[PushMessage.KEY_MATCH_ID]
        val chatId = data[PushMessage.KEY_CHAT_ID]
        return when {
            type in clubAnnouncementTypes && clubId != null -> PushDestination.ClubAnnouncement(clubId)
            type == TEAMS_PUBLISHED && matchId != null && clubId != null -> PushDestination.TeamsDrawn(matchId, clubId)
            type == MATCH_COMPLETED && matchId != null && clubId != null -> PushDestination.Match(matchId, clubId)
            type in clubTypes && clubId != null -> PushDestination.ClubAnnouncement(clubId)
            type in appTypes && data[PushMessage.KEY_URL]?.isWebUrl() == true -> PushDestination.External(data.getValue(PushMessage.KEY_URL))
            // Chat pushes predating `type` only carried `chatId`
            (type == NEW_MESSAGE || type == null) && chatId != null -> PushDestination.Chat(chatId)
            else -> PushDestination.None
        }
    }

    /** Deep link registered in the navigation graph for each destination. */
    fun deepLink(destination: PushDestination): String? = when (destination) {
        is PushDestination.ClubAnnouncement -> "squadfy://club/${destination.clubId}/announcement"
        is PushDestination.Match -> "squadfy://match/${destination.matchId}?clubId=${destination.clubId}"
        is PushDestination.TeamsDrawn -> "squadfy://match/${destination.matchId}/draw?clubId=${destination.clubId}"
        is PushDestination.Chat -> "squadfy://chat_details/${destination.chatId}"
        // Not a screen of the app: the platform opens it (MainActivity, IosPushBridge)
        is PushDestination.External, PushDestination.None -> null
    }

    fun deepLink(data: Map<String, String>): String? = deepLink(route(data))

    fun isChat(data: Map<String, String>): Boolean = route(data) is PushDestination.Chat

    /** The store or web page a push of the app opens, if any. */
    fun externalUrl(data: Map<String, String>): String? = (route(data) as? PushDestination.External)?.url

    /** The notification channel of a push type; must match `channelOf` in `firebase/functions/src/notifications/push.ts`. */
    fun channelOf(data: Map<String, String>): PushChannel {
        val type = data[PushMessage.KEY_TYPE]
        return when {
            isChat(data) -> PushChannel.CHAT
            type in clubTypes -> PushChannel.CLUB
            type in appTypes -> PushChannel.APP
            else -> PushChannel.MATCH_UPDATES
        }
    }

    /** A «new version» push for a version this install already has: received in the foreground, it is not shown. */
    fun isStaleUpdate(data: Map<String, String>, installedVersion: String): Boolean {
        if (data[PushMessage.KEY_TYPE] != APP_UPDATE) return false
        val version = data[KEY_VERSION]?.takeIf { AppUpdatePolicy.parse(it) != null } ?: return false
        return AppUpdatePolicy.status(installed = installedVersion, config = AppVersionConfig(latestVersion = version)) == UpdateStatus.UP_TO_DATE
    }

    private const val KEY_VERSION = "version"

    // Only web links: a push must never open another app's scheme
    private fun String.isWebUrl() = startsWith("https://")
}

/** Android notification channels (spec 009, 021), so each kind can be silenced on its own in the system settings. */
enum class PushChannel(val id: String) {
    MATCH_UPDATES("match_updates"),
    CHAT("chat"),
    CLUB("club"),
    APP("app")
}
