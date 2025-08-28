package com.pardip.quizmaster.ui.quiz.logic

/** Snap value to the nearest step boundary starting from [start]. */
fun snapToStep(value: Int, start: Int, step: Int): Int {
    val s = step.coerceAtLeast(1)
    val diff = value - start
    return start + ((diff + s / 2) / s) * s  // nearest, not floor
}