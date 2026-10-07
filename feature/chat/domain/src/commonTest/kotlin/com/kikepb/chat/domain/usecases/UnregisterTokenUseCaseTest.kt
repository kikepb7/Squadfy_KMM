package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeDeviceTokenService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UnregisterTokenUseCaseTest {

    private val service = FakeDeviceTokenService()
    private val useCase = UnregisterTokenUseCase(deviceTokenService = service)

    @Test
    fun `GIVEN valid token WHEN unregisterToken THEN delegates to service`() = runTest {
        service.unregisterTokenResult = Result.Success(Unit)

        val result = useCase.unregisterToken(token = "device-token-123")

        assertIs<Result.Success<*>>(result)
        assertEquals("device-token-123", service.lastUnregisteredToken)
    }

    @Test
    fun `GIVEN service fails WHEN unregisterToken THEN returns error`() = runTest {
        service.unregisterTokenResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = useCase.unregisterToken(token = "bad-token")

        assertIs<Result.Failure<*>>(result)
    }
}
