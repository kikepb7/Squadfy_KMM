package com.kikepb.auth.presentation.register

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import app.cash.turbine.test
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.domain.usecase.AuthRegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private class FakeAuthRepository(private val verified: Boolean) : AuthRepository {
        override suspend fun login(email: String, password: String): Result<AuthInfoModel, DataError.Remote> = TODO("not used")
        override suspend fun register(username: String, email: String, password: String): Result<Boolean, DataError.Remote> = Result.Success(verified)
        override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> = TODO("not used")
        override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> = TODO("not used")
        override suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote> = TODO("not used")
        override suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote> = TODO("not used")
        override suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote> = TODO("not used")
        override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> = TODO("not used")
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private suspend fun registerWith(verified: Boolean): RegisterEvent.Success {
        val viewModel = RegisterViewModel(AuthRegisterUseCase(FakeAuthRepository(verified)))
        viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("player1")
        viewModel.state.value.emailTextState.setTextAndPlaceCursorAtEnd("player1@squadfy.test")
        viewModel.state.value.passwordTextState.setTextAndPlaceCursorAtEnd("Password123!")
        var success: RegisterEvent.Success? = null
        viewModel.events.test {
            viewModel.onAction(RegisterAction.OnRegisterClick)
            success = awaitItem() as RegisterEvent.Success
        }
        return success!!
    }

    @Test
    fun `AC-002-18 with verification on the success screen asks to verify the email`() = runTest(UnconfinedTestDispatcher()) {
        assertEquals(RegisterEvent.Success(email = "player1@squadfy.test", alreadyVerified = false), registerWith(verified = false))
    }

    @Test
    fun `AC-002-18 with verification off the account is ready to log in`() = runTest(UnconfinedTestDispatcher()) {
        assertEquals(RegisterEvent.Success(email = "player1@squadfy.test", alreadyVerified = true), registerWith(verified = true))
    }
}
