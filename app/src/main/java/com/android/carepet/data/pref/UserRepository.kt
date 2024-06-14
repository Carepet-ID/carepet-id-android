package com.android.carepet.data.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.carepet.data.api.ApiService
import com.android.carepet.data.response.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository private constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>,
    private val sharedPreferences: SharedPreferences
) {
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val PASSWORD_KEY = stringPreferencesKey("password")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")
    private val PHOTO_KEY = stringPreferencesKey("photo")

    private val TOKEN_PREF_KEY = "token"

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    suspend fun loginUser(username: String, password: String): LoginResponse {
        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())

        val rawResponse = apiService.loginRaw(usernamePart, passwordPart)
        val rawResponseBody = rawResponse.body()?.string()

        val response = apiService.login(usernamePart, passwordPart)

        val cookies = rawResponse.headers().get("Set-Cookie")

        if (cookies.isNullOrEmpty()) {
            throw Exception("Token not found")
        }

        // Extract token from cookies
        val token = extractTokenFromCookie(cookies)
        if (token.isNullOrEmpty()) {
            throw Exception("Token not found")
        }

        val user = UserModel(
            username = username,
            password = password,
            token = token,
            isLogin = true
        )
        saveSession(user)
        saveTokenToPreferences(token)

        return response
    }

    private fun extractTokenFromCookie(cookie: String): String? {
        val tokenPattern = "token=([^;]+)".toRegex()
        val matchResult = tokenPattern.find(cookie)
        return matchResult?.groups?.get(1)?.value
    }

    suspend fun registerUser(username: String, email: String, password: String, role: String? = "user"): SignupResponse {
        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())
        val rolePart = (role ?: "user").toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.register(usernamePart, emailPart, passwordPart, rolePart)
    }

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = user.username
            preferences[EMAIL_KEY] = user.email
            preferences[PASSWORD_KEY] = user.password
            preferences[ROLE_KEY] = user.role
            preferences[PHOTO_KEY] = user.photo
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = user.isLogin
        }
    }

    private fun saveTokenToPreferences(token: String) {
        sharedPreferences.edit().putString(TOKEN_PREF_KEY, token).apply()
    }

    fun getTokenFromPreferences(): String? {
        return sharedPreferences.getString(TOKEN_PREF_KEY, null)
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                username = preferences[USERNAME_KEY] ?: "",
                email = preferences[EMAIL_KEY] ?: "",
                password = preferences[PASSWORD_KEY] ?: "",
                role = preferences[ROLE_KEY] ?: "",
                photo = preferences[PHOTO_KEY] ?: "",
                token = preferences[TOKEN_KEY] ?: "",
                isLogin = preferences[IS_LOGIN_KEY] ?: false
            )
        }
    }

    suspend fun getProfileDetail(): ProfileDetailResponse {
        val token = dataStore.data.map { it[TOKEN_KEY] }.firstOrNull() ?: ""
        if (token.isBlank()) {
            throw Exception("Token is null or empty")
        }
        return apiService.getProfileDetail("Bearer $token")
    }

    suspend fun logout() {
        val token = dataStore.data.map { it[TOKEN_KEY] }.firstOrNull() ?: ""

        if (token.isNotBlank()) {
            val response = apiService.logout("Bearer $token")
        }

        dataStore.edit { preferences ->
            preferences.clear()
        }

        sharedPreferences.edit().clear().apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(apiService: ApiService, dataStore: DataStore<Preferences>, sharedPreferences: SharedPreferences): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(apiService, dataStore, sharedPreferences).also { INSTANCE = it }
            }
        }
    }
}