package com.sharebite.app.ui.merchant

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// 限制 DatePicker 只能选今天或之后的日期（不能选过去）
@OptIn(ExperimentalMaterial3Api::class)
object FutureOnlySelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val todayStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return utcTimeMillis >= todayStart
    }
}

/**
 * Checks if the given date + time is already in the past.
 * Supports both merchant format (yyyy-MM-dd HH:mm:ss) and charity format (yyyy-MM-dd h:mm a).
 *
 * @param dateStr date as "yyyy-MM-dd"
 * @param timeStr time as either "HH:mm:ss" (merchant) or "h:mm a" (charity)
 * @return true if the combined date/time is before the current moment, false otherwise
 */
fun isDateTimeInPast(dateStr: String, timeStr: String): Boolean {
    if (dateStr.isBlank() || timeStr.isBlank()) return false

    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",   // Merchant format (24‑hour)
        "yyyy-MM-dd h:mm a"      // Charity format (12‑hour)
    )
    for (pattern in patterns) {
        try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.isLenient = false
            val parsed = formatter.parse("$dateStr $timeStr")
            if (parsed != null) return parsed.before(Date())
        } catch (_: Exception) {
            // try the next pattern
        }
    }
    return false // if parsing fails, treat as not past (safe fallback)
}

// 取得目前的日期字符串（今天）
fun getTodayDateString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Calendar.getInstance().time)
}

// 将各种时间格式统一转为 "HH:mm:ss" 供数据库使用
fun formatTimeForDb(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return "00:00:00"

    // 1. 处理 ISO 8601 格式（例如 "2026-09-01T17:30:00"）
    if (trimmed.contains('T')) {
        return try {
            val cleaned = trimmed.substringBefore('Z').substringBefore('+')
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(cleaned)
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            "00:00:00"
        }
    }

    // 2. 处理 12小时制（例如 "5:30 PM" 或 "05:30 PM"）
    if (trimmed.contains(Regex("(?i)[AP]M"))) {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = sdf.parse(trimmed.uppercase())
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            "00:00:00"
        }
    }

    // 3. 处理 "HH:mm" 或 "H:mm"（没有秒数，自动补 :00）
    if (trimmed.matches(Regex("^\\d{1,2}:\\d{2}$"))) {
        val parts = trimmed.split(":")
        return "${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}:00"
    }

    // 4. 如果已经是 "HH:mm:ss" 则直接返回，否则回退
    return trimmed
}