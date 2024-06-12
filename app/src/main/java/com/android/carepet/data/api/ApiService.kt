package com.android.carepet.data.api

import com.android.carepet.data.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("login")
    suspend fun login(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
    ): LoginResponse

    @Multipart
    @POST("login")
    suspend fun loginRaw(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): retrofit2.Response<ResponseBody>

    @Multipart
    @POST("signup")
    suspend fun register(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("role") role: RequestBody
    ): SignupResponse

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String?): LogoutResponse

    @Multipart
    @POST("predict")
    suspend fun predict(
        @Part imageFile: MultipartBody.Part,
        @Header("Authorization") token: String
    ): FileUploadResponse

    @Multipart
    @POST("disease")
    suspend fun detectDisease(
        @Part imageFile: MultipartBody.Part,
        @Header("Authorization") token: String
    ): DiseaseResponse

    @GET("disease/detail/{id}")
    suspend fun getDiseaseById(@Path("id") id: String): DiseaseResponse
}
