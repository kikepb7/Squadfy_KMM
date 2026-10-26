package com.kikepb.core.domain.auth.repository

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.SignInMethod
import com.kikepb.core.domain.auth.model.SocialCredential
import com.kikepb.core.domain.auth.model.SocialSignInResult
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthInfoModel, DataError.Remote>
    /** Success carries `hasVerifiedEmail`: true when the backend has email verification switched off (backend spec 009). */
    suspend fun register(username: String, email: String, password: String): Result<Boolean, DataError.Remote>
    suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote>
    suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote>
    suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote>
    suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote>
    suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote>
    suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote>
    /** `DELETE /me` (backend spec 010): immediate and irreversible. A wrong password is [DataError.Remote.UNAUTHORIZED]. */
    suspend fun deleteAccount(password: String): EmptyResult<DataError.Remote>

    // Third-party sign-in (spec 019 AC-019-23). The own backend has none: these defaults are never reached there,
    // because its build offers no provider.

    /** Signs in with a provider; the first time, the username is still to be chosen. */
    suspend fun signInWithProvider(credential: SocialCredential): Result<SocialSignInResult, DataError.Remote> =
        Result.Failure(DataError.Remote.BAD_REQUEST)

    /** Reserves the username of an account signed in with a provider and creates its profile (409 = taken). */
    suspend fun completeProfile(username: String): Result<AuthInfoModel, DataError.Remote> =
        Result.Failure(DataError.Remote.BAD_REQUEST)

    /** Leaving the username step deletes the half-created account, so nothing stays without a profile. */
    suspend fun abandonIncompleteAccount(): EmptyResult<DataError.Remote> = Result.Success(Unit)

    /** How the signed-in user proves who they are again: null = password. */
    fun currentSignInMethod(): SignInMethod? = null

    /** Deletion for accounts without password: re-authenticates with the provider's [credential]. */
    suspend fun deleteAccount(credential: SocialCredential): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.BAD_REQUEST)
}