package com.sharebite.app.data.session

import com.sharebite.app.data.model.AppUser

object CurrentUser {
    var user: AppUser? = null

    fun isLoggedIn(): Boolean = user != null

    fun logout() {
        user = null
    }
}