package com.kikepb.core.domain.validation

object PasswordValidator {

    private const val MIN_PASSWORD_LENGTH = 9

    fun validate(password: String): PasswordValidationState =
        PasswordValidationState(
            hasMinLength = password.length >= MIN_PASSWORD_LENGTH,
            hasDigit = password.any { it.isDigit() },
            hasUppercase = password.any { it.isUpperCase() }
        )
}