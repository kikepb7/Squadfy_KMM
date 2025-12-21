package com.kikepb.core.data.database

import androidx.sqlite.SQLiteException
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

suspend inline fun <T> safeDatabaseUpdate(update: suspend () -> T): Result<T, DataError.Local> {
    return try {
        Result.Success(data = update())
    } catch (_: SQLiteException) {
        Result.Failure(error = DataError.Local.DISK_FULL)
    }
}