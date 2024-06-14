package com.android.carepet.dashboard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.repository.ArticleRepository
import com.android.carepet.data.response.Article
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ArticleViewModel(private val repository: ArticleRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")

    val articles: Flow<PagingData<Article>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllArticles()
            } else {
                repository.searchArticles(query)
            }
        }
        .cachedIn(viewModelScope)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class ArticleViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            val apiService = ApiConfig.getApiService(context)
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel(ArticleRepository(apiService, context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
