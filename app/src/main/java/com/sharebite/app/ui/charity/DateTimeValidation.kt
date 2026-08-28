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
        val format = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault())
        val selected = format.parse("$dateStr $timeStr")
        val now = Calendar.getInstance().time
        selected != null && selected.before(now)
    } catch (e: Exception) {
        false // 格式解析失败就不拦截，避免因为格式问题误挡使用者
    }
}

// 取得目前的日期字符串（今天）
fun getTodayDateString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Calendar.getInstance().time)
}