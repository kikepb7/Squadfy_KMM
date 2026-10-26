package com.kikepb.core.firebase.auth

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.firebase.firestore.userDocument
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * The session is Firebase Auth's own (persisted and refreshed by the SDK, spec 019 AC-019-04), joined with the public
 * profile in `users/{uid}`, so a new picture or username reaches every screen at once. There are no tokens to store:
 * [AuthInfoModel.accessToken] and [AuthInfoModel.refreshToken] stay empty. Clearing the session signs out.
 */
class FirebaseSessionStorage(
    private val auth: FirebaseAuth,
    private val firestore: Lazy<FirebaseFirestore>
) : SessionStorage {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAuthInfo(): Flow<AuthInfoModel?> =
        auth.authStateChanged
            .onStart { emit(auth.currentUser) }
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                val user = auth.currentUser
                if (uid == null || user == null) {
                    flowOf(null)
                } else {
                    firestore.value.userDocument(uid).snapshots
                        .map { snapshot ->
                            val profile = if (snapshot.exists) snapshot.data(UserProfileDto.serializer()) else null
                            user.toAuthInfo(profile)
                        }
                        // Offline or before the profile exists: the session is still valid
                        .catch { emit(user.toAuthInfo(profile = null)) }
                        .onStart { emit(user.toAuthInfo(profile = null)) }
                }
            }
            .distinctUntilChanged()

    override suspend fun set(info: AuthInfoModel?) {
        // Firebase keeps the session itself; only clearing it means something
        if (info == null) auth.signOut()
    }
}

internal fun FirebaseUser.toAuthInfo(profile: UserProfileDto?) = AuthInfoModel(
    accessToken = "",
    refreshToken = "",
    user = UserModel(
        id = uid,
        email = email.orEmpty(),
        username = profile?.username ?: displayName.orEmpty(),
        hasVerifiedEmail = isEmailVerified,
        profilePictureUrl = profile?.pictureUrl ?: photoURL
    )
)
