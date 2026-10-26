package com.kikepb.core.firebase.firestore

import dev.gitlive.firebase.firestore.Timestamp
import kotlin.time.Instant

fun Timestamp.toInstant(): Instant =
    Instant.fromEpochSeconds(epochSeconds = seconds, nanosecondAdjustment = nanoseconds)

fun Instant.toTimestamp(): Timestamp =
    Timestamp(seconds = epochSeconds, nanoseconds = nanosecondsOfSecond)
