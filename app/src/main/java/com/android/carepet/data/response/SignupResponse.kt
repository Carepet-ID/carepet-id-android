package com.android.carepet.data.response

import com.google.gson.annotations.SerializedName

data class SignupResponse(

	@field:SerializedName("request")
	val request: Request,

	@field:SerializedName("response")
	val response: List<Any>,

	@field:SerializedName("name")
	val name: String
)
