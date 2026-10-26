package com.kikepb.core.firebase.error

import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FirebaseErrorMapperTest {

    @Test
    fun `AC-019-03 the backend code of a callable error is read from its details map`() {
        assertEquals("BANNED_FROM_CLUB", backendCodeOf(mapOf("code" to "BANNED_FROM_CLUB")))
        assertEquals(BackendErrorCode.BANNED_FROM_CLUB, BackendErrorCode.from(backendCodeOf(mapOf("code" to "BANNED_FROM_CLUB"))))
    }

    @Test
    fun `AC-019-03 the backend code is also found in the string form of the details`() {
        assertEquals("INVALID_INVITATION_CODE", backendCodeOf("{code=INVALID_INVITATION_CODE}"))
        assertEquals("USER_EXISTS", backendCodeOf("""{"code":"USER_EXISTS"}"""))
    }

    @Test
    fun `AC-019-03 details without a code give no backend code`() {
        assertNull(backendCodeOf(mapOf("reason" to "x")))
        assertNull(BackendErrorCode.from(backendCodeOf("nothing here")))
    }

    @Test
    fun `AC-019-03 non Firebase errors map to the generic remote errors`() {
        assertEquals(DataError.Remote.SERIALIZATION, SerializationException("bad").toRemoteError())
        assertEquals(DataError.Remote.BAD_REQUEST, IllegalArgumentException("bad").toRemoteError())
        assertEquals(DataError.Remote.UNKNOWN, IllegalStateException("x").toRemoteError())
        assertEquals(DataError.Remote.UNKNOWN, IllegalStateException("x").toRemoteErrorWithCode().status)
    }
}
