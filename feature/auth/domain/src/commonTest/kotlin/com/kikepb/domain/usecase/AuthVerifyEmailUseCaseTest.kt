package com.kikepb.domain.usecase

import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.Error
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthVerifyEmailUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val useCase = AuthVerifyEmailUseCase(authRepository = authRepository)

    @Test
    fun `GIVEN a valid token WHEN verifyEmail THEN returns Success`() = runTest {
        // GIVEN
        authRepository.verifyEmailResult = Success(data = Unit)

        // WHEN
        val result = useCase.verifyEmail(token = "valid-token-abc123")

        // THEN
        assertIs<Success<Unit>>(value = result)
    }

    @Test
    fun `GIVEN an invalid or expired token WHEN verifyEmail THEN returns Failure with UNAUTHORIZED`() = runTest {
        // GIVEN
        authRepository.verifyEmailResult = Failure(error = UNAUTHORIZED)

        // WHEN
        val result = useCase.verifyEmail(token = "expired-token")

        // THEN
        assertIs<Failure<Error>>(result)
        assertEquals(UNAUTHORIZED, (result as Failure).error)
    }
}
