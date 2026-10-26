package com.kikepb.chat.datafirebase.repository

import com.kikepb.chat.domain.repository.presence.PresenceRepository
import com.kikepb.core.firebase.error.safeFirebaseCall
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.database.ServerValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import kotlin.time.Clock

/**
 * Presence on Realtime Database (spec 019 AC-019-16, `database.rules.json`):
 * - `presence/{uid}`: online while a connection is open; the server marks it offline when it drops (`onDisconnect`).
 * - `typing/{chatId}/{uid}`: the time I last typed; stale entries (over [TYPING_TTL_MS]) do not count.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebasePresenceRepository(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    appScope: CoroutineScope
) : PresenceRepository {

    init {
        // While signed in, every (re)connection marks me online and arms the server-side offline mark
        auth.authStateChanged
            .onStart { emit(auth.currentUser) }
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) flowOf(null to false)
                else database.reference(".info/connected").valueEvents.map { uid to (it.value<Boolean?>() == true) }
            }
            .onEach { (uid, connected) ->
                if (uid == null || !connected) return@onEach
                safeFirebaseCall {
                    val ref = database.reference("presence/$uid")
                    ref.onDisconnect().setValue(PresenceDto.serializer(), PresenceDto(online = false, lastSeen = ServerValue.TIMESTAMP))
                    ref.setValue(PresenceDto.serializer(), PresenceDto(online = true, lastSeen = ServerValue.TIMESTAMP))
                }
            }
            .catch { }
            .launchIn(appScope)
    }

    override fun observeOnline(userId: String): Flow<Boolean> =
        database.reference("presence/$userId").valueEvents
            .map { snapshot -> snapshot.child("online").value<Boolean?>() == true }
            .catch { emit(false) }

    override fun observeTyping(chatId: String): Flow<Set<String>> =
        database.reference("typing/$chatId").valueEvents
            .map { snapshot ->
                val now = Clock.System.now().toEpochMilliseconds()
                val me = auth.currentUser?.uid
                snapshot.children
                    .filter { it.key != me && (it.value<Double?>()?.toLong() ?: 0L) > now - TYPING_TTL_MS }
                    .mapNotNull { it.key }
                    .toSet()
            }
            .catch { emit(emptySet()) }

    override suspend fun setTyping(chatId: String, typing: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        safeFirebaseCall {
            val ref = database.reference("typing/$chatId/$uid")
            if (typing) {
                // A killed app cannot clear it: the server does, and readers also drop marks older than the TTL
                ref.onDisconnect().removeValue()
                ref.setValue(Clock.System.now().toEpochMilliseconds().toDouble())
            } else {
                ref.removeValue()
            }
        }
    }

    private companion object {
        const val TYPING_TTL_MS = 8_000L
    }
}

@Serializable
private data class PresenceDto(val online: Boolean, val lastSeen: ServerValue)
