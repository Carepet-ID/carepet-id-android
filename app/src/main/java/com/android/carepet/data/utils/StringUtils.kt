package com.android.carepet.data.utils

object StringUtils {
    fun truncateDescription(description: String, maxLength: Int = 75): String {
        return if (description.length > maxLength) {
            "${description.substring(0, maxLength)}..."
        } else {
            description
        }
    }
}