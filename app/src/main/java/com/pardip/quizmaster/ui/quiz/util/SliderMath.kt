package com.pardip.quizmaster.ui.quiz.util

import kotlin.math.floor
import kotlin.math.round

fun clamp(value: Double, a: Double, b: Double): Double {
    val min = minOf(a, b)
    val max = maxOf(a, b)
    return value.coerceIn(min, max)
}

fun snapToStep(value: Double, start: Double, step: Double): Double {
    val s = if (step > 0.0) step else 1.0 // guard,
    val n = round((value - start) / s)
    return start + n * s
}

fun sliderSteps(start: Double, end: Double, step: Double): Int {
    val s = if (step > 0.0) step else 1.0
    val min = minOf(start, end)
    val max = maxOf(start, end)
    val intervals = floor((max - min) / s).toInt()
    return (intervals - 1).coerceAtLeast(0)
}
