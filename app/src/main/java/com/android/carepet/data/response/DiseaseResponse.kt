package com.android.carepet.data.response

import com.google.gson.annotations.SerializedName

data class DiseasetResponse(

	@field:SerializedName("item")
	val item: List<ItemItem>,

	@field:SerializedName("name")
	val name: String
)


data class ItemItem(

	@field:SerializedName("request")
	val request: Request,

	@field:SerializedName("response")
	val response: List<Any>,

	@field:SerializedName("name")
	val name: String
)


