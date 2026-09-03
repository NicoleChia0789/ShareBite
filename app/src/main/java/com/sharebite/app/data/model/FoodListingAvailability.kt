package com.sharebite.app.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun FoodListing.isExpired(now: Date = Date()): Boolean {
    if (deadline.isBlank()) return false
    val normalized = deadline.trim()
        .substringBefore('+')
        .removeSuffix("Z")
        .replace(Regex("T\\s*"), " ")
    val patterns = listOf("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd")
    val parsed = patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized)
        }.getOrNull()
    }
    return parsed?.before(now) ?: false
}
