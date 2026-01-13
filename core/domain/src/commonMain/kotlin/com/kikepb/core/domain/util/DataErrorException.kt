package com.kikepb.core.domain.util

class DataErrorException(
    val error: DataError
): Exception()