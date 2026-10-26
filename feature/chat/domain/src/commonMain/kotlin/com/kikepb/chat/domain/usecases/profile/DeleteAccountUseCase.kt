package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.repository.chat.ChatRepository
import com.kikepb.core.domain.auth.model.SignInMethod
import com.kikepb.core.domain.auth.model.SocialCredential
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.onSuccess

/**
 * Spec 011 AC-011-07: deletes the account on the backend (`DELETE /me`, backend spec 010) and, only if that
 * succeeds, wipes the local session and cached chats like a logout. Push devices are removed by the backend.
 */
class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(password: String): EmptyResult<DataError.Remote> =
        authRepository.deleteAccount(password = password).onSuccess {
            sessionStorage.set(info = null)
            chatRepository.deleteAllChats()
        }

    /** Spec 019 AC-019-23: accounts created with Google/Apple confirm with the provider instead of a password. */
    suspend operator fun invoke(credential: SocialCredential): EmptyResult<DataError.Remote> =
        authRepository.deleteAccount(credential = credential).onSuccess {
            sessionStorage.set(info = null)
            chatRepository.deleteAllChats()
        }

    /** How the signed-in user confirms: null = password. */
    val signInMethod: SignInMethod? get() = authRepository.currentSignInMethod()
}
