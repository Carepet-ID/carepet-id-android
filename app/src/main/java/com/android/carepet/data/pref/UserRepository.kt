package com.android.carepet.data.pref

import android.util.Log
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
    private val dataStore: DataStore<Preferences>
) {

    // Kunci Preferences
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val PASSWORD_KEY = stringPreferencesKey("password")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")
    private val PHOTO_KEY = stringPreferencesKey("photo")

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    suspend fun loginUser(username: String, password: String): LoginResponse {
        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())

        // Log response mentah
        val rawResponse = apiService.loginRaw(usernamePart, passwordPart)
        val rawResponseBody = rawResponse.body()?.string()
        Log.d("UserRepository", "Raw login response: $rawResponseBody")

        // Parse response menggunakan Retrofit
        val response = apiService.login(usernamePart, passwordPart)
        Log.d("UserRepository", "Login response: $response")

        val request = response.request
        val body = request?.body
        val formdata = body?.formdata

        if (request == null || body == null || formdata == null) {
            Log.e("UserRepository", "Login response has null fields: request=$request, body=$body, formdata=$formdata")
            // Sebagai pengganti melempar exception, kembalikan response error atau tangani sesuai kebutuhan
            return LoginResponse(request = null, response = null, name = null)
        }

        val role = formdata.find { it.key == "role" }?.value ?: ""
        val photo = formdata.find { it.key == "photo" }?.src ?: ""
        val token = formdata.find { it.key == "token" }?.value ?: ""

        val user = UserModel(
            username = username,
            password = password,
            role = role,
            photo = photo,
            token = token,
            isLogin = true
        )
        saveSession(user)

        return response
    }

    suspend fun registerUser(username: String, email: String, password: String, role: String? = "user"): SignupResponse {
        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())
        val rolePart = (role ?: "user").toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.register(usernamePart, emailPart, passwordPart, rolePart)
    }

    init {
        Log.d("UserRepository", "UserRepository initialized with apiService: $apiService and dataStore: $dataStore")
    }

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = user.username
            preferences[EMAIL_KEY] = user.email
            preferences[PASSWORD_KEY] = user.password
            preferences[ROLE_KEY] = user.role
            preferences[PHOTO_KEY] = user.photo
            preferences[TOKEN_KEY] = user.token ?: ""
            preferences[IS_LOGIN_KEY] = user.isLogin
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                username = preferences[USERNAME_KEY] ?: "",
                email = preferences[EMAIL_KEY] ?: "",
                password = preferences[PASSWORD_KEY] ?: "",
                role = preferences[ROLE_KEY] ?: "",
                photo = preferences[PHOTO_KEY] ?: "",
                token = preferences[TOKEN_KEY],
                isLogin = preferences[IS_LOGIN_KEY] ?: false
            )
        }
    }

    suspend fun logout() {
        val token = dataStore.data.map { it[TOKEN_KEY] }.firstOrNull() ?: ""
        Log.d("UserRepository", "Logout token: $token") // Log untuk memastikan token tidak kosong

        if (token.isNotBlank()) {
            val response = apiService.logout("Bearer $token")
            Log.d("UserRepository", "Logout response: $response") // Log respons logout untuk debugging
        } else {
            Log.e("UserRepository", "Token is blank or null")
        }

        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[IS_LOGIN_KEY] ?: false
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(apiService: ApiService, dataStore: DataStore<Preferences>): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(apiService, dataStore).also { INSTANCE = it }
            }
        }
    }
}


