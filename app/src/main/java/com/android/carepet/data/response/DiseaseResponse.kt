package com.android.carepet.data.response

import com.google.gson.annotations.SerializedName

data class DiseaseResponse(

	@field:SerializedName("item")
	val item: List<ItemItem>,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("category")
	val category: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("symptoms")
	val symptoms: String,

	@field:SerializedName("treatment")
	val treatment: String
)


data class ItemItem(

	@field:SerializedName("request")
	val request: Request,

	@field:SerializedName("response")
	val response: List<Any>,

	@field:SerializedName("name")
	val name: String
)


