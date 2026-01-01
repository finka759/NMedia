package ru.netology.nmedia.dao

import com.google.android.datatransport.runtime.dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.db.AppDb


@InstallIn(SingletonComponent::class)
@Module
object DaoModule {

    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()
}