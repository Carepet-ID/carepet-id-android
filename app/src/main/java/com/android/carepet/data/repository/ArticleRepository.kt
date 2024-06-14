package com.android.carepet.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import com.android.carepet.data.api.ApiService
import com.android.carepet.data.response.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ArticleRepository(private val apiService: ApiService, private val context: Context) {

    private val bookmarks = mutableListOf<Article>()

    private fun getToken(): String {
        val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    fun getAllArticles(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { ArticlePagingSource(apiService, getToken()) }
        ).flow
    }

    fun searchArticles(query: String): Flow<PagingData<Article>> {
        return getAllArticles().map { pagingData ->
            pagingData.filter { article ->
                article.title.contains(query, ignoreCase = true) || article.content.contains(query, ignoreCase = true)
            }
        }
    }

    fun getBookmarkedArticles(): Flow<List<Article>> {
        return flow {
            emit(bookmarks)
        }
    }

    suspend fun addBookmark(article: Article) {
        bookmarks.add(article)
    }

    suspend fun removeBookmark(article: Article) {
        bookmarks.remove(article)
    }
}
