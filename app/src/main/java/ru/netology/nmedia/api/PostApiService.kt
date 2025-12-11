package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token

//private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
////internal const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
//
//
//
//
//private val client = OkHttpClient.Builder()
//
//    // Интерцептор аутентификации из первого примера
//    .addInterceptor { chain ->
//        // Проверяем наличие токена перед каждым запросом
//        AppAuth.getInstance().data.value?.token?.let { token ->
//            // Если токен есть, создаем новый запрос с заголовком Authorization
//            val newRequest = chain.request()
//                .newBuilder()
//                .addHeader("Authorization", token)
//                .build()
//            // Продолжаем выполнение с новым запросом
//            return@addInterceptor chain.proceed(newRequest)
//        }
//        // Если токена нет, выполняем исходный запрос без изменений
//        chain.proceed(chain.request())
//    }
//    .apply {
//        if (BuildConfig.DEBUG) {
//            addInterceptor(HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY // Подробное логирование
//            })
//        }
//    }
//    .build()
//
//private val retrofit = Retrofit.Builder()
//    .addConverterFactory(GsonConverterFactory.create())
//    .baseUrl(BASE_URL)
//    .client(client)
//    .build()

interface PostApiService {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

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


//object PostApi {
//    val service: PostApiService by lazy {
//        retrofit.create(PostApiService::class.java)
//    }
//
//    val authService: AuthService by lazy { // Добавлен authService
//        retrofit.create(AuthService::class.java)
//    }
//}