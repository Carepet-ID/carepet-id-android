package com.android.carepet.data.response

data class ArticleResponse(
	val request: Request? = null,
	val response: List<Article>? = null,
	val name: String? = null
)

data class Article(
	val id: String,
	val title: String,
	val content: String,
	val category: String,
	val author: String,
	val createdAt: String,
	val photo: String
)