package com.pardip.quizmaster.ui.quiz.logic

import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Emits remaining milliseconds until [total] elapses.
 * [nowMs] is injectable for tests.
 */
fun countdownFlow(
    total: Duration,
    tick: Duration = 100.milliseconds,
    nowMs: () -> Long = { SystemClock.elapsedRealtime() }
): Flow<Long> =
    flow {
        val end = nowMs() + total.inWholeMilliseconds
        while (true) {
            val remaining = (end - nowMs()).coerceAtLeast(0)
            emit(remaining)
            if (remaining == 0L) break
            delay(tick)
        }
    }.distinctUntilChanged()