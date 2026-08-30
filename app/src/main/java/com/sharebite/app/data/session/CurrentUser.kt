package com.sharebite.app.data.session

import com.sharebite.app.data.model.AppUser

object CurrentUser {
    var user: AppUser? = null
        set(value) {
            field = value
            println("🔍 CurrentUser set: ${value?.name} (ID: ${value?.id}, Role: ${value?.role})")
        }

    fun getUserId(): String = user?.id ?: ""
    fun getUserName(): String = user?.name ?: ""
    fun getUserEmail(): String = user?.email ?: ""
    fun getUserRole(): String = user?.role ?: ""

    fun isLoggedIn(): Boolean {
        val loggedIn = user != null
        println("🔍 isLoggedIn: $loggedIn, user: ${user?.name}")
        return loggedIn
    }

    fun logout() {
        println("🔍 CurrentUser logged out")
        user = null
    }
}