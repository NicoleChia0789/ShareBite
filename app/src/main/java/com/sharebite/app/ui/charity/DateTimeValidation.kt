package com.sharebite.app.ui.charity

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import java.text.SimpleDateFormat
import java.util.Calendar
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

// 检查「选好的日期 + 时间」组合起来，是不是已经是过去的时间点
// dateStr 格式："yyyy-MM-dd"，timeStr 格式："h:mm a"（例如 "5:30 PM"）
fun isDateTimeInPast(dateStr: String, timeStr: String): Boolean {
    if (dateStr.isBlank() || timeStr.isBlank()) return false
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US)
        val selected = format.parse("$dateStr $timeStr")
        val now = Calendar.getInstance().time
        selected != null && selected.before(now)
    } catch (e: Exception) {
        false // 格式解析失败就不拦截，避免因为格式问题误挡使用者
    }
}

// 取得目前的日期字符串（今天）
fun getTodayDateString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return format.format(Calendar.getInstance().time)
}

// 新增：将各种时间格式统一转为 "HH:mm:ss" 供数据库使用
fun formatTimeForDb(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return "00:00:00"

    // 1. 处理 ISO 8601 格式（例如 "2026-09-01T17:30:00"）
    if (trimmed.contains('T')) {
        return try {
            val cleaned = trimmed.substringBefore('Z').substringBefore('+')
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = sdf.parse(cleaned)
            SimpleDateFormat("HH:mm:ss", Locale.US).format(date)
        } catch (e: Exception) {
            "00:00:00"
        }
    }

    // 2. 处理 12小时制（例如 "5:30 PM" 或 "05:30 PM"）
    // ✅ 修正：使用 RegexOption.IGNORE_CASE 代替 ignoreCase 布尔参数
    if (trimmed.contains(Regex("(?i)[AP]M"))) {  // 或 Regex("[AP]M", RegexOption.IGNORE_CASE)
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val date = sdf.parse(trimmed.uppercase())
            SimpleDateFormat("HH:mm:ss", Locale.US).format(date)
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

fun isDateTimeWithinDeadline(dateStr: String, timeStr: String, deadline: String): Boolean {
    if (dateStr.isBlank() || timeStr.isBlank() || deadline.isBlank()) return true
    val normalizedDeadline = deadline.trim()
        .substringBefore('+')
        .removeSuffix("Z")
        .replace(Regex("T\\s*"), " ")
    val deadlineDate = listOf("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm")
        .firstNotNullOfOrNull { pattern -> runCatching { SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }.parse(normalizedDeadline) }.getOrNull() }
        ?: return true
    val selectedDateTime = listOf("yyyy-MM-dd h:mm a", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm")
        .firstNotNullOfOrNull { pattern -> runCatching { SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }.parse("$dateStr $timeStr") }.getOrNull() }
        ?: return false
    return !selectedDateTime.after(deadlineDate)
}
