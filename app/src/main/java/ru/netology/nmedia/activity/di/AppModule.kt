package ru.netology.nmedia.activity.di

import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Указываем, что эти зависимости живут как синглтоны в приложении
object AppModule {

    /**
     * Предоставляет синглтон FirebaseMessaging.
     * Hilt вызовет FirebaseMessaging.getInstance() автоматически.
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    /**
     * Предоставляет синглтон GoogleApiAvailability.
     * Hilt вызовет GoogleApiAvailability.getInstance() автоматически.
     */
    @Provides
    @Singleton
    fun provideGoogleApiAvailability(): GoogleApiAvailability = GoogleApiAvailability.getInstance()
}