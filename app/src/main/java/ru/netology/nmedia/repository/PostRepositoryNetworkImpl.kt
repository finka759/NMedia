package ru.netology.nmedia.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map

import okio.IOException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError


class PostRepositoryNetworkImpl(
    private val dao: PostDao
) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map(PostEntity::toDto)
    }

    override fun isEmpty() = dao.isEmpty()


//    override suspend fun getAllAsync() {
//        Log.d("MyTag", "Repository.getAllAsync(): STARTING NETWORK REQUEST") // Лог 1
//        val posts = PostApi.service.getAll()
//        Log.d("MyTag", "Repository.getAllAsync(): NETWORK SUCCESS. Received ${posts.size} posts.") // Лог 2
//        dao.insert(posts.map(PostEntity::fromDto))
//        Log.d("MyTag", "Repository.getAllAsync(): FINISHED.") // Лог 7
//    }


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

        val post = dao.getPostById(id) ?: throw Exception("Post not found in DB")

        val localUpdatedPostEntity = post.copy(
            likeByMe = !likeByMe,
            likeCount = if (likeByMe) post.likeCount - 1 else post.likeCount + 1
        )
        dao.insert(localUpdatedPostEntity)

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
            Log.e("MyTag", "Repository.like(): Network error for Post ID: $id. Reverting local change.", e)

            // Возвращаем исходный пост в БД, отменяя локальное изменение.
            // Это гарантирует, что список постов в UI вернется к исходному состоянию.
            dao.insert(post)

            // Перебросываем исключение, чтобы ViewModel знала об ошибке и показала Snackbar
            throw e
        }

    }

}