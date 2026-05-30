package com.kikepb.domain.usecase

import com.kikepb.core.domain.util.DataError.Remote
import com.kikepb.core.domain.util.DataError.Remote.SERVER_ERROR
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResendEmailVerificationUseCaseTest {

    private val fakeAuthRepository = FakeAuthRepository()
    private val useCase = ResendEmailVerificationUseCase(authRepository = fakeAuthRepository)

    @Test
    fun `GIVEN a registered email WHEN invoke THEN returns Success`() = runTest {
        // GIVEN
        fakeAuthRepository.resendVerificationResult = Success(data = Unit)

        // WHEN
        val result = useCase.invoke(email = "user@example.com")

        // THEN
        assertIs<Success<Unit>>(value = result)
    }

    @Test
    fun `GIVEN a server error WHEN invoke THEN returns Failure`() = runTest {
        fakeAuthRepository.resendVerificationResult = Failure(error = SERVER_ERROR)

        val result = useCase.invoke(email = "user@example.com")

        assertIs<Failure<Remote>>(value = result)
        assertEquals(SERVER_ERROR, result.error)
    }
}
