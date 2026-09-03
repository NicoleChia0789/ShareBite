package com.sharebite.app.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 所有角色（Merchant, User, Charity）共用的 TextField 颜色配置
 *
 * 使用方式：
 * OutlinedTextField(
 *     ...,
 *     colors = AppTextFieldDefaults.colors()
 * )
 */
object AppTextFieldDefaults {

    /**
     * 默认的 OutlinedTextField 颜色配置
     * 文字颜色为深色（TextDark），标签和边框使用主题色
     */
    @Composable
    fun colors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            // ── 边框颜色 ──
            focusedBorderColor = OrangeAccent,
            unfocusedBorderColor = Color(0xFFDDC0B8),
            errorBorderColor = RedCancelled,

            // ── 文字颜色（修复打字颜色浅的问题） ──
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            errorTextColor = RedCancelled,

            // ── 标签颜色 ──
            focusedLabelColor = OrangeAccent,
            unfocusedLabelColor = TextGrey,
            errorLabelColor = RedCancelled,

            // ── 容器背景颜色 ──
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color(0xFFFFF3F3),

            // ── 光标颜色（修正：使用 cursorColor，不是 focusedCursorColor） ──
            cursorColor = OrangeAccent,
            errorCursorColor = RedCancelled,

            // ── 占位符颜色 ──
            focusedPlaceholderColor = TextGrey,
            unfocusedPlaceholderColor = TextGrey,
            errorPlaceholderColor = TextGrey,

            // ── 前缀/后缀颜色 ──
            focusedPrefixColor = TextGrey,
            unfocusedPrefixColor = TextGrey,
            errorPrefixColor = RedCancelled,
            focusedSuffixColor = TextGrey,
            unfocusedSuffixColor = TextGrey,
            errorSuffixColor = RedCancelled
        )
    }

    /**
     * 搜索框专用颜色配置（背景稍不同，圆角更大）
     */
    @Composable
    fun searchColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangeAccent,
            unfocusedBorderColor = Color(0xFFDDC0B8),
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedLabelColor = OrangeAccent,
            unfocusedLabelColor = TextGrey,
            cursorColor = OrangeAccent,
            errorCursorColor = RedCancelled,
            focusedPlaceholderColor = TextGrey,
            unfocusedPlaceholderColor = TextGrey,
            errorBorderColor = RedCancelled,
            errorTextColor = RedCancelled
        )
    }

    /**
     * 错误状态专用颜色配置（红色边框 + 浅红背景）
     */
    @Composable
    fun errorColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RedCancelled,
            unfocusedBorderColor = RedCancelled,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            errorBorderColor = RedCancelled,
            errorTextColor = RedCancelled,
            errorLabelColor = RedCancelled,
            focusedContainerColor = Color(0xFFFFF3F3),
            unfocusedContainerColor = Color(0xFFFFF3F3),
            cursorColor = RedCancelled,
            errorCursorColor = RedCancelled,
            focusedPlaceholderColor = TextGrey,
            unfocusedPlaceholderColor = TextGrey,
            errorPlaceholderColor = TextGrey
        )
    }
}