package com.android.carepet.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

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

    fun getDataStore(): DataStore<Preferences> = dataStore

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val PHOTO_KEY = stringPreferencesKey("photo")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")

        fun getInstance(context: Context): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(context.dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
