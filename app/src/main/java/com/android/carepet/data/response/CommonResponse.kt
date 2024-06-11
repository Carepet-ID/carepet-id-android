package com.android.carepet.data.response

import com.google.gson.annotations.SerializedName

data class FormdataItem(

    @field:SerializedName("type")
    val type: String,

    @field:SerializedName("value")
    val value: String,

    @field:SerializedName("key")
    val key: String,

    @field:SerializedName("src")
    val src: String? = null
)

data class Url(

    @field:SerializedName("path")
    val path: List<String>,

    @field:SerializedName("protocol")
    val protocol: String,

    @field:SerializedName("host")
    val host: List<String>,

    @field:SerializedName("raw")
    val raw: String
)

data class Request(

    @field:SerializedName("method")
    val method: String,

    @field:SerializedName("header")
    val header: List<Any>,

    @field:SerializedName("body")
    val body: Body,

    @field:SerializedName("url")
    val url: Url
)

data class Body(

    @field:SerializedName("mode")
    val mode: String,

    @field:SerializedName("formdata")
    val formdata: List<FormdataItem>
)
