package com.example.recipehub.AppInterface


import com.example.recipehub.modle.Recipe
import com.example.recipehub.modle.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiInterface {
    @POST("api/v1/User/login")
    fun login(@Body param:Map<String,String>): Call<ResponseBody>

    @POST("api/v1/User/register")
    fun Signup(@Body param:Map<String,String>): Call<ResponseBody>

    @GET("api/v1/Recipe/list")
    fun ListRecipe(@Query("page") page: Int, @Header("Authorization") token: String ):Call<ResponseBody>

    @Multipart
    @POST("api/v1/User/update")
    fun updateProfile(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part?,  // Make file optional
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody
    ): Call<User>

    @Multipart
    @POST("api/v1/Recipe/createRecipes")
    fun createRecipe(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part?,
        @Part("title") title:RequestBody,
        @Part("description") description:RequestBody,
        @Part("ingredients") ingredients:RequestBody
    ):Call<Recipe>

    @GET("api/v1/Recipe/list/user")
    fun ListUserRecipe(@Query("page") page: Int, @Header("Authorization") token: String ):Call<ResponseBody>


    @POST("api/v1/Recipe/list/user/delete")
    fun DeleteUserRecipe(@Header("Authorization") token: String,@Body param:Map<String,String>):Call<ResponseBody>
}