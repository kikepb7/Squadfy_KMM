package com.kikepb.domain.usecase

import com.kikepb.core.domain.util.DataError.Remote
import com.kikepb.core.domain.util.DataError.Remote.CONFLICT
import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResetPasswordUseCaseTest {

    private val fakeAuthRepository = FakeAuthRepository()
    private val useCase = ResetPasswordUseCase(authRepository = fakeAuthRepository)

    @Test
    fun `GIVEN a valid token and new password WHEN resetPassword THEN returns Success`() = runTest {
        // GIVEN
        fakeAuthRepository.resetPasswordResult = Success(data = Unit)

        // WHEN
        val result = useCase.resetPassword(newPassword = "NewPassword1", token = "valid-token")

        // THEN
        assertIs<Success<Unit>>(value = result)
    }

    @Test
    fun `GIVEN an expired token WHEN resetPassword THEN returns Failure with UNAUTHORIZED`() = runTest {
        // GIVEN
        fakeAuthRepository.resetPasswordResult = Failure(error = UNAUTHORIZED)

        // WHEN
        val result = useCase.resetPassword(newPassword = "NewPassword1", token = "expired-token")

        // THEN
        assertIs<Failure<Remote>>(value = result)
        assertEquals(UNAUTHORIZED, result.error)
    }

    @Test
    fun `GIVEN same password as current WHEN resetPassword THEN returns Failure with CONFLICT`() = runTest {
        // GIVEN
        fakeAuthRepository.resetPasswordResult = Failure(error = CONFLICT)

        // WHEN
        val result = useCase.resetPassword(newPassword = "SamePassword1", token = "valid-token")

        // THEN
        assertIs<Failure<Remote>>(value = result)
        assertEquals(CONFLICT, result.error)
    }
}
