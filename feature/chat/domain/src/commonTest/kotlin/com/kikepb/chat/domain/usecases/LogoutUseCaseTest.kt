package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeAuthRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LogoutUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val useCase = LogoutUseCase(authRepository = authRepository)

    @Test
    fun `GIVEN valid refresh token WHEN logout THEN returns success and passes token`() = runTest {
        authRepository.logoutResult = Result.Success(Unit)

        val result = useCase.logout(refreshToken = "refresh-token")

        assertIs<Result.Success<*>>(result)
        assertEquals("refresh-token", authRepository.lastLogoutRefreshToken)
    }

    @Test
    fun `GIVEN repository fails WHEN logout THEN returns error`() = runTest {
        authRepository.logoutResult = Result.Failure(error = DataError.Remote.UNAUTHORIZED)

        val result = useCase.logout(refreshToken = "bad-token")

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
    }
}
