package com.kikepb.domain.validation

import com.kikepb.core.domain.validation.PasswordValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordValidatorTest {

    @Test
    fun `GIVEN a password meeting all requirements WHEN validate THEN isValidPassword is true`() {
        // GIVEN
        val password ="Password1"

        // WHEN
        val result = PasswordValidator.validate(password = password)

        // THEN
        assertTrue(result.isValidPassword)
    }

    @Test
    fun `GIVEN a password with minimum length WHEN validate THEN hasMinLength is true`() {
        // GIVEN
        val password = "Password1"

        // WHEN
        val result = PasswordValidator.validate(password = password)

        // THEN
        assertTrue(result.hasMinLength)
    }

    @Test
    fun `GIVEN a password shorter than 9 chars WHEN validate THEN hasMinLength is false`() {
        // GIVEN
        val password = "Pass1"

        // WHEN
        val result = PasswordValidator.validate(password = password)

        // THEN
        assertFalse(result.hasMinLength)
    }

    @Test
    fun `GIVEN a password without uppercase WHEN validate THEN hasUppercase is false and isValidPassword is false`() {
        // GIVEN
        val password = "password1"

        // WHEN
        val result = PasswordValidator.validate(password = password)

        // THEN
        assertFalse(result.hasUppercase)
        assertFalse(result.isValidPassword)
    }

    @Test
    fun `GIVEN a password without digit WHEN validate THEN hasDigit is false and isValidPassword is false`() {
        // GIVEN
        val password = "PasswordOnly"

        // WHEN
        val result = PasswordValidator.validate(password = password)

        // THEN
        assertFalse(result.hasDigit)
        assertFalse(result.isValidPassword)
    }

    @Test
    fun `GIVEN an empty password WHEN validate THEN all checks are false`() {
        // GIVEN
        val password = ""

        // WHEN
        val result = PasswordValidator.validate(password = password)

        // THEN
        assertFalse(result.hasMinLength)
        assertFalse(result.hasDigit)
        assertFalse(result.hasUppercase)
        assertFalse(result.isValidPassword)
    }

    @Test
    fun `GIVEN a strong password WHEN validate THEN all individual checks are true`() {
        // GIVEN
        val password = "SecurePass9"

        // WHEN
        val result = PasswordValidator.validate(password = password)

        // THEN
        assertTrue(result.hasMinLength)
        assertTrue(result.hasDigit)
        assertTrue(result.hasUppercase)
    }
}
