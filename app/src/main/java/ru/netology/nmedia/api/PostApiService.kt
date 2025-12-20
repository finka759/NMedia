package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token

interface PostApiService {
    @GET("posts")
    suspend fun getAll(): List<Post>


    //начальная загрузка, последняя страница с бекенда
    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    //возращает страницу с постами относительно передпнного id
    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    //возращает страницу с постами относительно передпнного id
    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Post

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Media

    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body pushToken: PushToken): Response<Unit>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun signIn(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<Token>

}

// Добавляем интерфейс AuthService сюда же
//interface AuthService {
//    @FormUrlEncoded
//    @POST("users/authentication")
//    suspend fun signIn(
//        @Field("login") login: String,
//        @Field("pass") pass: String
//    ): Response<Token>
//}


