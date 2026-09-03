package com.sharebite.app.util

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDateTimeForDisplay(value: String?): String {
    if (value.isNullOrBlank()) return "Not available"
    val normalized = value.trim().substringBefore('+').removeSuffix("Z").replace(Regex("T\\s*"), " ")
    val patterns = listOf("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd")
    for (pattern in patterns) {
        val parsed = runCatching { SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized) }.getOrNull() ?: continue
        return SimpleDateFormat(if (pattern == "yyyy-MM-dd") "d MMM yyyy" else "d MMM yyyy, h:mm a", Locale.US).format(parsed)
    }
    return value.replace('T', ' ')
}
