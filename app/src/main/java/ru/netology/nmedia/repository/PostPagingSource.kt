package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError

class PostPagingSource(
    private val apiService: PostApiService
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? {
        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {


            val result = when (params) {
                is LoadParams.Refresh -> {
                    android.util.Log.d("PagingLog", "Loading REFRESH (initial load)")
                    apiService.getLatest(params.loadSize)
                }
                is LoadParams.Append -> {
                    android.util.Log.d("PagingLog", "Loading APPEND with key: ${params.key}")
                    apiService.getBefore(id = params.key, count = params.loadSize)
                }

                is LoadParams.Prepend -> return LoadResult.Page(
                    data = emptyList(), nextKey = null, prevKey = null,
                )//prevKey = params.key,
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


            // --- ВОТ ЗДЕСЬ СТАВИМ ЛОГИ ДЛЯ ПРОВЕРКИ ДАННЫХ ---
            android.util.Log.d("PagingLog", "Received data size: ${data.size}")
            if (data.isNotEmpty()) {
                data.forEach { post ->
                    android.util.Log.d("PagingLog", "Post ID: ${post.id}, Content: ${post.content.take(30)}...")
                }
            } else {
                android.util.Log.d("PagingLog", "The list from server is EMPTY")
            }
            // ------------------------------------------------


            // Если данных нет, nextKey должен быть null, чтобы пагинация остановилась
//            val nextKey = data.lastOrNull()?.id
            val nextKey = if (data.isEmpty()) null else data.last().id


            return LoadResult.Page(
                data,
                prevKey = null,
//                prevKey = params.key,
                nextKey = nextKey,
            )

        }catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }

            return LoadResult.Error(e)
        }
    }
}
