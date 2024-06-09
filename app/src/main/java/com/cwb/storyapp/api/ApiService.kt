package com.cwb.storyapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @GET("stories")
    fun getAllStories(): Call<GetAllStoriesResponse>

    @GET("stories/{id}")
    fun getStoryDetail(@Path("id") id: String): Call<DetailStoryResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Part description: MultipartBody.Part,
        @Part photo: MultipartBody.Part?,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?

    ): Call<AddStoryResponse>

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Query("location") location: Int = 1
    ): GetAllStoriesResponse

    @GET("stories")
    suspend fun getAllStoriesWithPaging(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): GetAllStoriesResponse
}

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val loginResult: LoginResult
)

data class RegisterResponse(
    val error: Boolean,
    val message: String
)

data class LoginResult(
    val userId: String,
    val name: String,
    val token: String
)

data class GetAllStoriesResponse(
    val error: Boolean,
    val message: String,
    val listStory: List<Story>
)

data class DetailStoryResponse(
    val error: Boolean,
    val message: String,
    val story: Story
)

data class Story(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
    val lat: Double,
    val lon: Double
)

data class AddStoryResponse(
    val error: Boolean,
    val message: String
)