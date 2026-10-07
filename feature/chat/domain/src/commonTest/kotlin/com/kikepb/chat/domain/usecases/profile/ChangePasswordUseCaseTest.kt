package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.fake.FakeAuthRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChangePasswordUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val useCase = ChangePasswordUseCase(authRepository = authRepository)

    @Test
    fun `GIVEN valid passwords WHEN changePassword THEN returns success`() = runTest {
        authRepository.changePasswordResult = Result.Success(Unit)

        val result = useCase.changePassword(
            currentPassword = "OldPass1",
            newPassword = "NewPass1"
        )

        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `GIVEN wrong current password WHEN changePassword THEN returns UNAUTHORIZED error`() = runTest {
        authRepository.changePasswordResult = Result.Failure(error = DataError.Remote.UNAUTHORIZED)

        val result = useCase.changePassword(
            currentPassword = "WrongPass",
            newPassword = "NewPass1"
        )

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
    }

    @Test
    fun `GIVEN same current and new password WHEN changePassword THEN returns CONFLICT error`() = runTest {
        authRepository.changePasswordResult = Result.Failure(error = DataError.Remote.CONFLICT)

        val result = useCase.changePassword(
            currentPassword = "SamePass1",
            newPassword = "SamePass1"
        )

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.CONFLICT, (result as Result.Failure).error)
    }
}
