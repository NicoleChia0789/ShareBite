package com.sharebite.app.ui.merchant

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MerchantPickupRange { ALL, TODAY, THIS_WEEK, UPCOMING }

fun matchesMerchantPickupRange(dateText: String, timeText: String, range: MerchantPickupRange): Boolean {
    val date = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply { isLenient = false }.parse(dateText)
    }.getOrNull() ?: return false
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val pickupDay = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return when (range) {
        MerchantPickupRange.ALL -> true
        MerchantPickupRange.TODAY ->
            pickupDay.timeInMillis == today.timeInMillis && !isDateTimeInPast(dateText, timeText)
        MerchantPickupRange.THIS_WEEK -> {
            val end = today.clone() as Calendar
            end.add(Calendar.DAY_OF_YEAR, 7)
            pickupDay.after(today) && pickupDay.before(end)
        }
        MerchantPickupRange.UPCOMING -> {
            val end = today.clone() as Calendar
            end.add(Calendar.DAY_OF_YEAR, 7)
            pickupDay.after(end)
        }
    }
}
