package ru.netology.nmedia.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import javax.inject.Inject


class PostRepositoryNetworkImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService
) : PostRepository {

    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource(
                apiService
            )
        },
    ).flow


//    override val data = dao.getAllVisible().map { it.map { it.toDto() } }

    override fun isEmpty() = dao.isEmpty()

    // Отметить все невидимые посты как видимые
    override suspend fun showAllInvisible() {
        dao.showAllInvisible()
    }

    override suspend fun getAllAsync() {
        Log.d("MyTag", "Repository.getAllAsync(): STARTING NETWORK REQUEST")
        try {
//            val posts = PostApi.service.getAll()
            val posts = apiService.getAll()
            Log.d(
                "MyTag",
                "Repository.getAllAsync(): NETWORK SUCCESS. Received ${posts.size} posts."
            )
            if (posts.isNotEmpty()) { // проверка, чтобы не вызывать insert для пустого списка
                // вставляем новые посты как НЕВИДИМЫЕ (false)
                dao.insert(posts.toEntity(isVisible = false))
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
//            val response = PostApi.service.save(post)
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            // Вставляем полученный (новый или обновленный) пост в локальную БД
            //Пост, созданный локально, сразу ВИДИМ (true)
            dao.insert(PostEntity.fromDto(body, isVisible = true))
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


    override suspend fun saveWithAttachment(post: Post, photo: File?) {
        try {
            val media = photo?.let {
                upload(it)
            }

            val postWithAttachment = post.copy(
                attachment = media?.let {
                    Attachment(it.id, type = AttachmentType.IMAGE)
                })


//            val response = PostApi.service.save(postWithAttachment)
            val response = apiService.save(postWithAttachment)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(PostEntity.fromDto(body, isVisible = true))


        } catch (e: IOException) {

            throw NetworkError
        } catch (e: Exception) {

            throw UnknownError
        }
    }

    private suspend fun upload(file: File): Media =
        apiService.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody(),
            )
        )


    override suspend fun removeById(id: Long) {
        val deletingPost = dao.getPostById(id)
        if (deletingPost != null) {
            dao.removeById(id)
            try {
                apiService.removeById(id)
            } catch (e: Exception) {
                dao.insert(deletingPost)
                throw e
            }
        }
    }

    override suspend fun like(
        id: Long,

    ): Post {
        val postInDb = dao.getPostById(id) ?: throw RuntimeException("Post not found in DB")
        val wasLiked = postInDb.likeByMe
        // Переключаем состояние
        dao.likeById(id)

        try {
            //  Отправляем запрос на сервер
            val postFromServer = if (wasLiked) {
                apiService.dislikeById(id)
            } else {
                apiService.likeById(id)
            }
            // При успешном ответе сервера, обновляем БД данными с сервера
            // (на случай расхождений, например, сервер вернул другое количество лайков)
//            dao.insert(PostEntity.fromDto(postFromServer))
            dao.insert(PostEntity.fromDto(postFromServer, isVisible = true))
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
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            // Вставляем свежие посты как НЕВИДИМЫЕ (false)
            dao.insert(body.toEntity(isVisible = false))
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

}