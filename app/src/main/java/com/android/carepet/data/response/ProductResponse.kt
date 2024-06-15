package com.android.carepet.data.response

data class Product(
	val id: String,
	val name: String,
	val category: String,
	val description: String,
	val price: Double,
	val photo: String,
	val linkStore: String
)

data class ProductResponse(
	val request: Request? = null,
	val response: List<Product>? = null,
	val name: String? = null
)