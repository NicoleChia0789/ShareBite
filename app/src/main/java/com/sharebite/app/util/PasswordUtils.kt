package com.sharebite.app.util

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordUtils {

    // 注册时：把明文密码加密
    fun hashPassword(plainPassword: String): String {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
    }

    // 登入时：比对明文密码跟加密后的结果是否相符
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        val result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword)
        return result.verified
    }
}