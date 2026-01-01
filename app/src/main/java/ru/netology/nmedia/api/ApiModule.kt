package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth


@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
    }

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Подробное логирование
    }
    @Singleton
    @Provides
    fun provideOkhttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth,
    ): OkHttpClient = OkHttpClient.Builder()

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
                addInterceptor(logging)
            }
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
    ): Retrofit =  Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()
    @Singleton
    @Provides
    fun provideApiService(
       retrofit: Retrofit,
    ) : PostApiService = retrofit.create()

}