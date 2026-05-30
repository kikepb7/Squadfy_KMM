package com.kikepb.domain.usecase

import com.kikepb.core.domain.util.DataError.Remote.NOT_FOUND
import com.kikepb.core.domain.util.Error
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ForgotPasswordUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = ForgotPasswordUseCase(repository)

    @Test
    fun `GIVEN a registered email WHEN forgotPassword THEN returns Success`() = runTest {
        // GIVEN
        repository.forgotPasswordResult = Success(data = Unit)

        // WHEN
        val result = useCase.forgotPassword(email = "user@example.com")

        // THEN
        assertIs<Success<Unit>>(value = result)
    }

    @Test
    fun `GIVEN a non-existing email WHEN forgotPassword THEN returns Failure with NOT_FOUND`() = runTest {
        // GIVEN
        repository.forgotPasswordResult = Failure(error = NOT_FOUND)

        // WHEN
        val result = useCase.forgotPassword(email = "unknown@example.com")

        // THEN
        assertIs<Failure<Error>>(value = result)
        assertEquals(NOT_FOUND, (result as Failure).error)
    }
}
