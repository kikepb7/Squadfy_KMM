package com.kikepb.domain.usecase

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.util.DataError.Remote
import com.kikepb.core.domain.util.DataError.Remote.FORBIDDEN
import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoginUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val useCase = LoginUseCase(authRepository = authRepository)

    @Test
    fun `GIVEN valid credentials WHEN login THEN returns Success with auth info`() = runTest {
        // GIVEN
        authRepository.loginResult = Success(data = FakeAuthRepository.defaultAuthInfoModel())

        // WHEN
        val result = useCase.login(email = "user@example.com", password = "Password1")

        // THEN
        assertIs<Success<AuthInfoModel>>(value = result)
        assertEquals("access-token", result.data.accessToken)
    }

    @Test
    fun `GIVEN invalid credentials WHEN login THEN returns Failure with UNAUTHORIZED`() = runTest {
        // GIVEN
        authRepository.loginResult = Failure(error = UNAUTHORIZED)

        // WHEN
        val result = useCase.login(email = "user@example.com", password = "wrongpassword")

        // THEN
        assertIs<Failure<Remote>>(value = result)
        assertEquals(UNAUTHORIZED, result.error)
    }

    @Test
    fun `GIVEN unverified email WHEN login THEN returns Failure with FORBIDDEN`() = runTest {
        // GIVEN
        authRepository.loginResult = Failure(error = FORBIDDEN)

        // WHEN
        val result = useCase.login(email = "unverified@example.com", password = "Password1")

        // THEN
        assertIs<Failure<Remote>>(value = result)
        assertEquals(FORBIDDEN, result.error)
    }
}
