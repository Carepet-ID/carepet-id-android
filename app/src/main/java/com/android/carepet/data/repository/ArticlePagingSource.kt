package com.android.carepet.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.carepet.data.api.ApiService
import com.android.carepet.data.response.Article
import retrofit2.HttpException
import java.io.IOException

class ArticlePagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val response = apiService.getAllArticles("Bearer $token")
            LoadResult.Page(
                data = response,
                prevKey = null,
                nextKey = null
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition
    }
}