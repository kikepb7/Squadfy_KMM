package com.kikepb.core.domain.notification

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
    }
}

/** Where tapping a push takes the user (AC-009-03). */
sealed interface PushDestination {
    data class ClubAnnouncement(val clubId: String) : PushDestination
    data class Match(val matchId: String, val clubId: String) : PushDestination
    data class Chat(val chatId: String) : PushDestination
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

    private val clubAnnouncementTypes = setOf(ANNOUNCEMENT_OPENED, ANNOUNCEMENT_CLOSING_SOON, WAITLIST_PROMOTED, MATCH_CANCELLED, MATCH_RESCHEDULED)

    fun route(data: Map<String, String>): PushDestination {
        val type = data[PushMessage.KEY_TYPE]
        val clubId = data[PushMessage.KEY_CLUB_ID]
        val matchId = data[PushMessage.KEY_MATCH_ID]
        val chatId = data[PushMessage.KEY_CHAT_ID]
        return when {
            type in clubAnnouncementTypes && clubId != null -> PushDestination.ClubAnnouncement(clubId)
            type == TEAMS_PUBLISHED && matchId != null && clubId != null -> PushDestination.Match(matchId, clubId)
            // Chat pushes predating `type` only carried `chatId`
            (type == NEW_MESSAGE || type == null) && chatId != null -> PushDestination.Chat(chatId)
            else -> PushDestination.None
        }
    }

    /** Deep link registered in the navigation graph for each destination. */
    fun deepLink(destination: PushDestination): String? = when (destination) {
        is PushDestination.ClubAnnouncement -> "squadfy://club/${destination.clubId}/announcement"
        is PushDestination.Match -> "squadfy://match/${destination.matchId}?clubId=${destination.clubId}"
        is PushDestination.Chat -> "squadfy://chat_details/${destination.chatId}"
        PushDestination.None -> null
    }

    fun deepLink(data: Map<String, String>): String? = deepLink(route(data))

    fun isChat(data: Map<String, String>): Boolean = route(data) is PushDestination.Chat
}
