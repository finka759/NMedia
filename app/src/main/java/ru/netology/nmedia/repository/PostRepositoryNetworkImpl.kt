package ru.netology.nmedia.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okio.IOException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import kotlin.collections.map


class PostRepositoryNetworkImpl(
    private val dao: PostDao
) : PostRepository {


//    // 1. Получение исходного Flow от DAO
//    val rawFlow: Flow<List<Entity>> = dao.getAll()
//
//    // 2. Операция map внешнего Flow (преобразование списка)
//// Мы определяем функцию-преобразователь, которую передаем в map:
//    val transformList: (List<Entity>) -> List<Dto> = { entityList ->
//        // Здесь мы применяем внутренний map к самому списку
//        entityList.map { entity ->
//            entity.toDto()
//        }
//    }
//
//    // 3. Применение функции преобразования к Flow
//    val listFlow: Flow<List<Dto>> = rawFlow.map { transformList(it) }
//
//    // 4. Применение контекста выполнения
//    override val data = listFlow.flowOn(Dispatchers.Default)


    override val data = dao.getAll().map { it.map { it.toDto() } }


    override fun isEmpty() = dao.isEmpty()


    override suspend fun getAllAsync() {
        Log.d("MyTag", "Repository.getAllAsync(): STARTING NETWORK REQUEST")
        try {
            val posts = PostApi.service.getAll()
            Log.d(
                "MyTag",
                "Repository.getAllAsync(): NETWORK SUCCESS. Received ${posts.size} posts."
            )
            if (posts.isNotEmpty()) { // проверка, чтобы не вызывать insert для пустого списка
                dao.insert(posts.map(PostEntity::fromDto))
                Log.d("MyTag", "Repository.getAllAsync(): INSERTED ${posts.size} posts into DB.")
            } else {
                Log.d("MyTag", "Repository.getAllAsync(): API returned 0 posts. Nothing to insert.")
            }
        } catch (e: Exception) {
            // обработка любых исключений
            Log.e(
                "MyTag",
                "Repository.getAllAsync(): AN EXCEPTION OCCURRED during network request or DB operation!",
                e
            )
            // Важно: перебросить исключение, чтобы ViewModel мог его обработать
            throw e
        }
        Log.d("MyTag", "Repository.getAllAsync(): FINISHED.")
    }


    override suspend fun save(post: Post): Post {
        try {
            val response = PostApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            // Вставляем полученный (новый или обновленный) пост в локальную БД
            dao.insert(PostEntity.fromDto(body))

            // Возвращаем объект Post в случае успешного выполнения
            return body

        } catch (e: IOException) {
            // Перехватываем ошибки ввода/вывода (например, проблемы с сетью)
            throw NetworkError
        } catch (e: Exception) {
            // Перехватываем все остальные исключения
            throw UnknownError
        }
    }


    override suspend fun removeById(id: Long) {
        val deletingPost = dao.getPostById(id)
        if (deletingPost != null) {
            dao.removeById(id)
            try {
                PostApi.service.removeById(id)
            } catch (e: Exception) {
                dao.insert(deletingPost)
                throw e
            }
        }
    }

    override suspend fun like(
        id: Long,
        likeByMe: Boolean
    ): Post {
        // Переключаем состояние
        dao.likeById(id)

        try {
            //  Отправляем запрос на сервер
            val postFromServer = if (likeByMe) {
                PostApi.service.dislikeById(id)
            } else {
                PostApi.service.likeById(id)
            }

            // При успешном ответе сервера, обновляем БД данными с сервера
            // (на случай расхождений, например, сервер вернул другое количество лайков)
            dao.insert(PostEntity.fromDto(postFromServer))

            // Возвращаем объект Post из сервера
            return postFromServer

        } catch (e: Exception) {
            // Если сетевой запрос провалился:
            Log.e(
                "MyTag",
                "Repository.like(): Network error for Post ID: $id. Reverting local change.",
                e
            )

            // Переключаем состояние обратно.
            // Это гарантирует, что список постов в UI вернется к исходному состоянию.
            dao.likeById(id)

            // Перебросываем исключение, чтобы ViewModel знала об ошибке и показала Snackbar
            throw e
        }
    }


    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

}