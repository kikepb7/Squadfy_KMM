package com.kikepb.domain.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {

    @Test
    fun `GIVEN a valid email WHEN validate THEN returns true`() = assertTrue(EmailValidator.validate(email = "user@example.com"))

    @Test
    fun `GIVEN a valid email with subdomain WHEN validate THEN returns true`() = assertTrue(EmailValidator.validate(email = "user@mail.example.com"))

    @Test
    fun `GIVEN a valid email with plus alias WHEN validate THEN returns true`() = assertTrue(EmailValidator.validate(email = "user+tag@example.com"))

    @Test
    fun `GIVEN an email without at symbol WHEN validate THEN returns false`() = assertFalse(EmailValidator.validate(email = "userexample.com"))

    @Test
    fun `GIVEN an email without domain WHEN validate THEN returns false`() = assertFalse(EmailValidator.validate(email = "user@"))

    @Test
    fun `GIVEN an email without TLD WHEN validate THEN returns false`() = assertFalse(EmailValidator.validate(email = "user@example"))

    @Test
    fun `GIVEN an email with spaces WHEN validate THEN returns false`() = assertFalse(EmailValidator.validate(email = "user @example.com"))

    @Test
    fun `GIVEN an empty string WHEN validate THEN returns false`() = assertFalse(EmailValidator.validate(email = ""))

    @Test
    fun `GIVEN an email with multiple at symbols WHEN validate THEN returns false`() = assertFalse(EmailValidator.validate(email = "user@@example.com"))
}
