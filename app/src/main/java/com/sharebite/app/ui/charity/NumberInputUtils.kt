package com.sharebite.app.ui.charity

import java.text.NumberFormat
import java.util.Locale

// 数量类栏位的合理上限，超过这个数字视为不合理（可依需求调整）
const val MAX_QUANTITY = 100_000

// 过滤使用者输入，只允许正整数（拦掉负号、小数点、字母），并限制最大长度避免输入超大数字
fun filterQuantityInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    // 最多允许 6 位数字（对应 MAX_QUANTITY = 100,000），避免打到天文数字
    return formatWithThousandSeparator(digitsOnly.take(6))
}

fun quantityValue(value: String): Int? = value.filter { it.isDigit() }.toIntOrNull()

// 验证数量是否合理：必须是正整数，且不超过上限
fun isQuantityValid(value: String): Boolean {
    val number = quantityValue(value) ?: return false
    return number in 1..MAX_QUANTITY
}

// 把数字加上千位分隔符方便阅读，例如 12000 -> "12,000"
fun formatWithThousandSeparator(value: String): String {
    val number = quantityValue(value) ?: return value
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

fun formatWithThousandSeparator(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}
