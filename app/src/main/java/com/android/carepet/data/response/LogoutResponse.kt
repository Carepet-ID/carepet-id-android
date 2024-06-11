package com.android.carepet.data.response

import com.google.gson.annotations.SerializedName

data class LogoutResponse(

	@field:SerializedName("request")
	val request: Request,

	@field:SerializedName("response")
	val response: List<Any>,

	@field:SerializedName("name")
	val name: String
)

data class Auth(

	@field:SerializedName("type")
	val type: String
)

