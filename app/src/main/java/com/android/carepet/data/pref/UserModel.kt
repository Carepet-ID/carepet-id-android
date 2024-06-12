package com.android.carepet.data.pref

data class UserModel(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val photo: String = "",
    val token: String = "",
    val isLogin: Boolean = false
)
