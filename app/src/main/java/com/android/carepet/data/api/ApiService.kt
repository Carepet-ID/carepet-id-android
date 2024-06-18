package com.android.carepet.data.api

import com.android.carepet.data.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
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

    @GET("disease")
    suspend fun getAllDiseases(@Header("Authorization") token: String): List<DiseaseResponse>

    @GET("disease/detail/{id}")
    suspend fun getDiseaseById(@Path("id") id: String): DiseaseResponse

    @GET("profile")
    suspend fun getProfileDetail(@Header("Authorization") token: String): ProfileDetailResponse

    @GET("article")
    suspend fun getAllArticles(@Header("Authorization") token: String): List<Article>

    @GET("article/{category}")
    suspend fun getArticleByCategory(@Header("Authorization") token: String, @Path("category") category: String): List<Article>

    @GET("article/detail/{id}")
    suspend fun getArticleById(@Header("Authorization") token: String, @Path("id") id: String): Article

    @GET("product")
    suspend fun getAllProducts(@Header("Authorization") token: String): List<Product>

    @GET("product/{category}")
    suspend fun getProductsByCategory(
        @Header("Authorization") token: String,
        @Path("category") category: String
    ): List<Product>

    @GET("dog")
    suspend fun getAllDogs(@Header("Authorization") token: String): List<DogResponse>

    @GET("dog/{id}")
    suspend fun getDogDetail(@Header("Authorization") token: String, @Path("id") id: String): DogResponse

    @Multipart
    @POST("dog")
    suspend fun addNewDog(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("about") about: RequestBody,
        @Part("age") age: RequestBody,
        @Part("birthday") birthday: RequestBody,
        @Part("breed") breed: RequestBody,
        @Part("skinColor") skinColor: RequestBody,
        @Part("gender") gender: RequestBody
    ): Response<DogResponse>

    @Multipart
    @PUT("dog/{id}")
    suspend fun updateDogDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part("name") name: RequestBody,
        @Part("gender") gender: RequestBody?,
        @Part("birthday") birthday: RequestBody?,
        @Part("age") age: RequestBody?,
        @Part("breed") breed: RequestBody?,
        @Part("about") about: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<DogResponse>

    @DELETE("dog/{id}")
    suspend fun deleteDog(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): DeleteDogResponse
}
