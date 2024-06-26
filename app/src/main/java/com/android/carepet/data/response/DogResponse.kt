package com.android.carepet.data.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DogResponse(
	val id: String,
	val photo: String,
	val name: String,
	val about: String,
	val age: Int,
	val birthday: String,
	val breed: String,
	val skinColor: String,
	val gender: String
) : Parcelable

data class DeleteDogResponse(
	val success: Boolean,
	val message: String
)



