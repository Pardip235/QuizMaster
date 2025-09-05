package com.pardip.quizmaster.ui.quiz.util

import kotlin.math.floor
import kotlin.math.round

/**
 * Clamps [value] into the closed interval defined by [a] and [b], regardless of their order.
 * @return a value in [min(a,b), max(a,b)]
 */
fun clamp(value: Double, a: Double, b: Double): Double {
    val min = minOf(a, b)
    val max = maxOf(a, b)
    return value.coerceIn(min, max)
}

/**
 * Snaps [value] to the nearest point of the arithmetic sequence: start, start+step, ...
 * If [step] <= 0, 1.0 is used.
 * @return start + n*step for some integer n
 */
fun snapToStep(value: Double, start: Double, step: Double): Double {
    val s = if (step > 0.0) step else 1.0 // guard,
    val n = round((value - start) / s)
    return start + n * s
}

/**
 * Computes the number of interior slider stops between [start] and [end] for spacing [step].
 * If [step] <= 0, 1.0 is used. Excludes endpoints. Result is >= 0.
 */
fun sliderSteps(start: Double, end: Double, step: Double): Int {
    val s = if (step > 0.0) step else 1.0
    val min = minOf(start, end)
    val max = maxOf(start, end)
    val intervals = floor((max - min) / s).toInt()
    return (intervals - 1).coerceAtLeast(0)
}
