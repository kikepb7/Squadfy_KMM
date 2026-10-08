package com.kikepb.chat.data.websocket.remote

import com.kikepb.chat.data.network.KtorWebSocketConnector
import com.kikepb.core.domain.realtime.ClubDataChange
import com.kikepb.core.domain.realtime.ClubDataScope
import com.kikepb.core.domain.realtime.ClubLiveUpdates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** `{type: "CLUB_DATA_CHANGED", payload: "{clubId, scope, matchId?}"}` (backend spec 012 RN-A). */
@Serializable
internal data class ClubDataChangedDTO(
    val clubId: String,
    val scope: String,
    val matchId: String? = null
)

internal const val CLUB_DATA_CHANGED = "CLUB_DATA_CHANGED"

class WebSocketClubLiveUpdates(
    private val webSocketConnector: KtorWebSocketConnector,
    private val json: Json
) : ClubLiveUpdates {

    override fun observe(clubId: String): Flow<ClubDataChange> =
        webSocketConnector.messages
            .filter { it.type == CLUB_DATA_CHANGED }
            .mapNotNull { message -> parseClubDataChange(json, message.payload) }
            .filter { it.clubId == clubId }
}

/** Unknown scopes (a newer backend) and malformed payloads are ignored instead of breaking the stream. */
internal fun parseClubDataChange(json: Json, payload: String): ClubDataChange? {
    val dto = runCatching { json.decodeFromString<ClubDataChangedDTO>(payload) }.getOrNull() ?: return null
    val scope = ClubDataScope.entries.firstOrNull { it.name == dto.scope } ?: return null
    return ClubDataChange(clubId = dto.clubId, scope = scope, matchId = dto.matchId)
}
