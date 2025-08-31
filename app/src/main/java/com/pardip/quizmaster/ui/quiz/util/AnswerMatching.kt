package com.pardip.quizmaster.ui.quiz.util

import java.text.Normalizer
import java.util.Locale

private val DIACRITICS = "\\p{Mn}+".toRegex()
private val NON_ALNUM = "[^a-z0-9\\s]".toRegex()
private val WS = "\\s+".toRegex()

/** Normalize an answer for fair, case/diacritics/punctuation-insensitive compare. */
fun normalizeAnswer(raw: String): String =
    Normalizer.normalize(raw.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .replace(NON_ALNUM, "")
        .replace(WS, " ")