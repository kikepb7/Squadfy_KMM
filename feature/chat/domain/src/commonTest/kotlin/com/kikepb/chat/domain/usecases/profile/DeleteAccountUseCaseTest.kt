package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.fake.FakeAuthRepository
import com.kikepb.chat.domain.fake.FakeChatRepository
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeleteAccountUseCaseTest {

    private class FakeSessionStorage(initial: AuthInfoModel?) : SessionStorage {
        val authInfo = MutableStateFlow(initial)
        override fun observeAuthInfo(): Flow<AuthInfoModel?> = authInfo
        override suspend fun set(info: AuthInfoModel?) { authInfo.value = info }
    }

    private val authRepository = FakeAuthRepository()
    private val chatRepository = FakeChatRepository()
    private val sessionStorage = FakeSessionStorage(initial = FakeAuthRepository.defaultAuthInfoModel())
    private val useCase = DeleteAccountUseCase(authRepository, sessionStorage, chatRepository)

    @Test
    fun `AC-011-07 deleting the account sends the password and clears the session and cached chats`() = runTest {
        val result = useCase(password = "Secret123")

        assertIs<Result.Success<Unit>>(result)
        assertEquals("Secret123", authRepository.lastDeleteAccountPassword)
        assertNull(sessionStorage.authInfo.value)
        assertTrue(chatRepository.deleteAllChatsCalled)
    }

    @Test
    fun `AC-011-07 a wrong password keeps the session and the local data`() = runTest {
        authRepository.deleteAccountResult = Result.Failure(DataError.Remote.UNAUTHORIZED)

        val result = useCase(password = "wrong")

        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
        assertNotNull(sessionStorage.authInfo.value)
        assertFalse(chatRepository.deleteAllChatsCalled)
    }
}
