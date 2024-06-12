package com.android.carepet.data.response

data class FileUploadResponse(
	val status: String,
	val message: String,
	val predict: Predict?
)

data class Predict(
	val id: String,
	val photo: String,
	val name: String,
	val accuracy: Double,
	val diseaseId: String
)



