package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetworkImpl

class DependencyContainer(
    private val context: Context
) {

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
        //internal const val BASE_URL = "http://10.0.2.2:9999/api/slow/"

        @Volatile
        private var instance: DependencyContainer? = null

        fun initApp(context: Context){
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer {
            return instance!!
        }
    }

    val appAuth = AppAuth(context)


    private val client = OkHttpClient.Builder()

        // Интерцептор аутентификации из первого примера
        .addInterceptor { chain ->
            // Проверяем наличие токена перед каждым запросом
            appAuth.data.value?.token?.let { token ->
                // Если токен есть, создаем новый запрос с заголовком Authorization
                val newRequest = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                // Продолжаем выполнение с новым запросом
                return@addInterceptor chain.proceed(newRequest)
            }
            // Если токена нет, выполняем исходный запрос без изменений
            chain.proceed(chain.request())
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY // Подробное логирование
                })
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    private val appDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    val apiService = retrofit.create<PostApiService>()

    private val postDao = appDb.postDao()

    val repository: PostRepository = PostRepositoryNetworkImpl(
        postDao,
        apiService
    )




}