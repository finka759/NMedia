package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {


    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>)
            : MediatorResult {

        try {
            val result = when (loadType) {
                LoadType.REFRESH ->
//                    apiService.getLatest(state.config.initialLoadSize)
                {
                    // Ищем ID самого нового поста
                    val maxId = postRemoteKeyDao.max()
                    if (maxId != null) {
                        // Если данные есть, запрашиваем только то, что новее (для Swipe-to-Refresh)
                        apiService.getAfter(maxId, state.config.pageSize)
                    } else {
                        // Если БД пуста (чистая установка), загружаем последние данные
                        apiService.getLatest(state.config.initialLoadSize)
                    }
                }


                LoadType.PREPEND -> {
//                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(
//                        endOfPaginationReached = false
//                    )
//                    apiService.getAfter(id, state.config.pageSize)
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getBefore(id, state.config.pageSize)
                }
            }
            if (!result.isSuccessful) {
                // ЛОГ ОШИБКИ СЕРВЕРА
                android.util.Log.e("PagingLog", "Error: ${result.code()} ${result.message()}")
                throw ApiError(result.code(), result.message())
            }
            val data = result.body() ?: throw ApiError(
                result.code(),
                result.message(),
            )

            appDb.withTransaction {

                when (loadType) {
                    LoadType.REFRESH -> {
//                        postDao.clear()

//                        postRemoteKeyDao.insert(
//                            listOf(
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.AFTER,
//                                    data.first().id
//                                ),
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.BEFORE,
//                                    data.last().id
//                                ),
//                            )
//                        )
                        if (data.isNotEmpty()) {
                            // Обновляем ключ AFTER только если пришли новые данные
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    data.first().id
                                )
                            )
                            // Если это была первая загрузка (БД пуста), ставим и BEFORE
                            if (postRemoteKeyDao.min() == null) {
                                postRemoteKeyDao.insert(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        data.last().id
                                    )
                                )
                            }
                        }
                    }

//                    LoadType.PREPEND -> {
//                        postRemoteKeyDao.insert(
//                            PostRemoteKeyEntity(
//                                PostRemoteKeyEntity.KeyType.AFTER,
//                                data.first().id
//                            )
//                        )
//                    }

                    LoadType.APPEND -> {
                        if (data.isNotEmpty()) {//добавил проверку на пустоту в бд
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    data.last().id
                                )
                            )
                        }

                    }

                    else -> Unit
                }

                if (BuildConfig.DEBUG) {
                    // --- ВОТ ЗДЕСЬ СТАВИМ ЛОГИ ДЛЯ ПРОВЕРКИ ДАННЫХ ---
                    android.util.Log.d("PagingLog", "Received data size: ${data.size}")
                    if (data.isNotEmpty()) {
                        data.forEach { post ->
                            android.util.Log.d(
                                "PagingLog",
                                "Post ID: ${post.id}, Content: ${post.content.take(30)}..."
                            )
                        }
                    } else {
                        android.util.Log.d("PagingLog", "The list from server is EMPTY")
                    }
                    // ------------------------------------------------
                }

                // Если данных нет, nextKey должен быть null, чтобы пагинация остановилась
//            val nextKey = data.lastOrNull()?.id
//                val nextKey = if (data.isEmpty()) null else data.last().id

//          postDao.insert(data.map{ PostEntity.fromDto(it)})
                postDao.insert(data.map(PostEntity::fromDto))

            }


            return MediatorResult.Success(
                data.isEmpty()
            )

        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }

            return MediatorResult.Error(e)
        }
    }


}
