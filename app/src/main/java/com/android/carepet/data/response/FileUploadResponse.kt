package com.android.carepet.data.response

data class FileUploadResponse(
	val item: List<ItemItem?>? = null,
	val name: String? = null
)

data class Url(
	val path: List<String?>? = null,
	val protocol: String? = null,
	val host: List<String?>? = null,
	val raw: String? = null
)

data class FormdataItem(
	val src: List<Any?>? = null,
	val type: String? = null,
	val key: String? = null
)

data class Request(
	val method: String? = null,
	val auth: Auth? = null,
	val header: List<Any?>? = null,
	val body: Body? = null,
	val url: Url? = null
)

data class Auth(
	val type: String? = null
)

data class ItemItem(
	val request: Request? = null,
	val response: List<Any?>? = null,
	val name: String? = null
)

data class Body(
	val mode: String? = null,
	val formdata: List<FormdataItem?>? = null
)

