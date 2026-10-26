package com.kikepb.club.datafirebase.repository

import com.kikepb.club.datafirebase.dto.LiveChangeDto
import com.kikepb.core.domain.realtime.ClubDataChange
import com.kikepb.core.domain.realtime.ClubDataScope
import com.kikepb.core.firebase.firestore.clubDocument
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.mapNotNull

/**
 * Live notices of a club (spec 019 AC-019-14) from `clubs/{clubId}/live/changes`, which the Functions update after each
 * change, replacing `CLUB_DATA_CHANGED` over the WebSocket. The first snapshot is the last change already seen.
 */
class FirebaseClubLiveUpdates(private val firestore: FirebaseFirestore) : com.kikepb.core.domain.realtime.ClubLiveUpdates {

    override fun observe(clubId: String): Flow<ClubDataChange> =
        firestore.clubDocument(clubId).collection("live").document("changes")
            .snapshots
            .drop(1)
            .mapNotNull { snapshot ->
                if (!snapshot.exists) return@mapNotNull null
                val change = snapshot.data(LiveChangeDto.serializer())
                val scope = ClubDataScope.entries.firstOrNull { it.name == change.scope } ?: return@mapNotNull null
                ClubDataChange(clubId = clubId, scope = scope, matchId = change.matchId)
            }
            // Denied once the user is no longer a member: no more notices
            .catch { }
}
