package com.kikepb.domain.usecase

import com.kikepb.core.domain.util.DataError.Remote.CONFLICT
import com.kikepb.core.domain.util.Error
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthRegisterUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val useCase = AuthRegisterUseCase(authRepository = authRepository)

    @Test
    fun `GIVEN valid registration data WHEN authRegister THEN returns Success`() = runTest {
        // GIVEN
        authRepository.registerResult = Success(data = Unit)

        // WHEN
        val result = useCase.authRegister(username = "newuser", email = "newuser@example.com", password = "Password1")

        // THEN
        assertIs<Success<Unit>>(result)
    }

    @Test
    fun `GIVEN already existing account WHEN authRegister THEN returns Failure with CONFLICT`() = runTest {
        // GIVEN
        authRepository.registerResult = Failure(error = CONFLICT)

        // WHEN
        val result = useCase.authRegister(username = "existinguser", email = "existing@example.com", password = "Password1")

        // THEN
        assertIs<Failure<Error>>(result)
        assertEquals(CONFLICT, (result as Failure).error)
    }
}
