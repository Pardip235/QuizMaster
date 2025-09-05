package com.pardip.quizmaster.data.converter

import com.pardip.quizmaster.data.model.KahootQuestion

fun resolveQuestionImageUrl(q: KahootQuestion, coverUrl: String?): String? {
    q.image.nonBlank()?.takeIf { it.isHttp() }?.let { return it }

    // Prefer explicit images/gifs
    q.media.firstOrNull { it.type == "image" || it.type == "gif" }?.let { m ->
        idToUrl(
            id = m.id,
            coverUrl = coverUrl,
            preferCoverIfMatches = false
        )?.let { return it }
    }

    // Then backgrounds (may reuse cover if same asset)
    q.media.firstOrNull { it.type == "background_image" }?.let { m ->
        idToUrl(
            id = m.id,
            coverUrl = coverUrl,
            preferCoverIfMatches = true
        )?.let { return it }
    }

    return coverUrl
}

private const val KAHOOT_CDN = "https://media.kahoot.it/"

private fun String?.nonBlank() = this?.trim()?.takeIf { it.isNotEmpty() }
private fun String?.isHttp() = this?.startsWith("http", ignoreCase = true) == true
private fun String?.assetId() =
    this?.substringAfterLast(delimiter = '/', missingDelimiterValue = "")
        ?.substringBefore(delimiter = '?', missingDelimiterValue = "")
        ?.nonBlank()

private fun idToUrl(
    id: String?,
    coverUrl: String?,
    preferCoverIfMatches: Boolean
): String? {
    val raw = id.nonBlank() ?: return null
    if (raw.isHttp()) return raw
    if (preferCoverIfMatches && coverUrl.assetId() == raw) return coverUrl
    return "$KAHOOT_CDN$raw"
}


