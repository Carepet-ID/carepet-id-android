package com.android.carepet.data.di

import android.content.Context
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.pref.UserPreference
import com.android.carepet.data.pref.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository = runBlocking {
        val pref = UserPreference.getInstance(context)
        val user = pref.getSession().first()
        val dataStore = UserPreference.getInstance(context).getDataStore()
        val apiService = ApiConfig.getApiService(context)
        UserRepository.getInstance(apiService, pref.getDataStore())
    }
}
