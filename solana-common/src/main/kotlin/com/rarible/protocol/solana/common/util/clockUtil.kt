package com.rarible.protocol.solana.common.util

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Clock.nowMillis(): Instant = instant().truncatedTo(ChronoUnit.MILLIS)
