package com.kikepb.core.firebase.auth

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.firebase.error.safeFirebaseCall
import com.kikepb.core.firebase.firestore.userDocument
import com.kikepb.core.firebase.functions.FunctionsClient
import com.kikepb.core.firebase.functions.NoInput
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore

/**
 * Firebase Auth with email and password (spec 019 AC-019-04…06), behind the same contract as the own backend.
 * - Registering creates the account, then `completeProfile` reserves the username (`usernames/{lower}`) and writes
 *   `users/{uid}`; a taken username undoes the account. The verification email is sent, but not required to sign in,
 *   as with the own backend in production (backend flag `email-verification` off).
 * - [verifyEmail] and [resetPassword] take the `oobCode` of the Firebase action link as their token.
 * - Changing the password or deleting the account re-authenticates with the current password first.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: Lazy<FirebaseFirestore>,
    private val functions: FunctionsClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthInfoModel, DataError.Remote> =
        safeFirebaseCall(mapError = { it.toAuthError() }) {
            val user = requireNotNull(auth.signInWithEmailAndPassword(email.trim(), password).user)
            val profile = runCatching {
                firestore.value.userDocument(user.uid).get().takeIf { it.exists }?.data(UserProfileDto.serializer())
            }.getOrNull()
            user.toAuthInfo(profile)
        }

    override suspend fun register(username: String, email: String, password: String): Result<Boolean, DataError.Remote> {
        val created = safeFirebaseCall(mapError = { it.toAuthError() }) {
            requireNotNull(auth.createUserWithEmailAndPassword(email.trim(), password).user)
        }
        val user = when (created) {
            is Result.Success -> created.data
            is Result.Failure -> return created
        }
        return when (val profile = functions.run("completeProfile", CompleteProfileRequest(username.trim()))) {
            is Result.Success -> {
                safeFirebaseCall { user.sendEmailVerification() }
                Result.Success(user.isEmailVerified)
            }
            is Result.Failure -> {
                // The username was taken (or the profile could not be written): no half-created account stays behind
                safeFirebaseCall { user.delete() }
                Result.Failure(
                    if (profile.error.code == BackendErrorCode.USER_EXISTS) DataError.Remote.CONFLICT else profile.error.status
                )
            }
        }
    }

    override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> =
        safeFirebaseCall(mapError = { it.toAuthError() }) {
            // Only the signed-in (just registered) user can ask again; otherwise there is nothing to send
            auth.currentUser?.sendEmailVerification()
            Unit
        }

    override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> =
        safeFirebaseCall(mapError = { it.toAuthError() }) {
            auth.applyActionCode(token)
            auth.currentUser?.reload()
            Unit
        }

    override suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote> =
        safeFirebaseCall(mapError = { it.toAuthError() }) {
            try {
                auth.sendPasswordResetEmail(email.trim())
            } catch (_: dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException) {
                // Same answer for unknown emails, so accounts cannot be enumerated (as the own backend does)
            }
        }

    override suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote> =
        safeFirebaseCall(mapError = { it.toAuthError() }) {
            auth.confirmPasswordReset(token, newPassword)
        }

    override suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote> {
        // The own backend answers 409 SAME_PASSWORD; Firebase does not check it
        if (currentPassword == newPassword) return Result.Failure(DataError.Remote.CONFLICT)
        return safeFirebaseCall(mapError = { it.toAuthError() }) {
            val user = requireNotNull(auth.currentUser) { "No session" }
            user.reauthenticate(EmailAuthProvider.credential(requireNotNull(user.email), currentPassword))
            user.updatePassword(newPassword)
        }
    }

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> =
        safeFirebaseCall { auth.signOut() }

    /**
     * Re-authenticates, then `deleteAccount` erases and anonymizes the user's data and deletes the Auth user on the
     * server (spec 010 rules, AC-019-06); the local session goes with it.
     */
    override suspend fun deleteAccount(password: String): EmptyResult<DataError.Remote> {
        val reauthenticated = safeFirebaseCall(mapError = { it.toAuthError() }) {
            val user = requireNotNull(auth.currentUser) { "No session" }
            user.reauthenticate(EmailAuthProvider.credential(requireNotNull(user.email), password))
        }
        if (reauthenticated is Result.Failure) return reauthenticated
        return when (val deleted = functions.run("deleteAccount", NoInput)) {
            is Result.Success -> safeFirebaseCall { auth.signOut() }
            is Result.Failure -> Result.Failure(deleted.error.status)
        }
    }
}
