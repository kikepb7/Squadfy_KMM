package com.kikepb.auth.presentation.email_verification

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.auth.presentation.fake.FakeAuthRepository
import com.kikepb.auth.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.usecase.AuthVerifyEmailUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmailVerificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
    }

    private fun createViewModel(token: String? = "valid-token") =
        EmailVerificationViewModel(
            authVerifyEmailUseCase = AuthVerifyEmailUseCase(authRepository),
            savedStateHandle = SavedStateHandle(mapOf("token" to token))
        )

    @Test
    fun `GIVEN a valid token WHEN state is collected THEN isVerified becomes true`() = runTest {
        // GIVEN
        authRepository.verifyEmailResult = Success(data = Unit)
        val viewModel = createViewModel(token = "valid-token")

        viewModel.state.test {
            // WHEN
            val initial = awaitItem()
            assertTrue(initial.isVerifying)

            // THEN
            val verified = awaitItem()
            assertFalse(verified.isVerifying)
            assertTrue(verified.isVerified)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN an invalid token WHEN state is collected THEN isVerified remains false`() = runTest {
        // GIVEN
        authRepository.verifyEmailResult = Failure(error = UNAUTHORIZED)
        val viewModel = createViewModel(token = "invalid-token")

        viewModel.state.test {
            // WHEN
            val initial = awaitItem()
            assertTrue(initial.isVerifying)

            // THEN
            val failed = awaitItem()
            assertFalse(failed.isVerifying)
            assertFalse(failed.isVerified)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN no token in SavedStateHandle WHEN state is collected THEN verification fails`() = runTest {
        // GIVEN
        authRepository.verifyEmailResult = Failure(error = UNAUTHORIZED)
        val viewModel = createViewModel(token = null)

        viewModel.state.test {
            // WHEN
            val initial = awaitItem()
            assertTrue(initial.isVerifying)

            // THEN
            val result = awaitItem()
            assertFalse(result.isVerified)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
