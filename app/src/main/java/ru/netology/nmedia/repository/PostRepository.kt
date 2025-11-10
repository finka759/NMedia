package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    fun isEmpty(): LiveData<Boolean>
    suspend fun getAllAsync()

    fun getNewerCount(id: Long): Flow<Int>

    suspend fun fetchAndSaveNewerPosts(id: Long): Int
    suspend fun save(post: Post): Post
    suspend fun removeById(id: Long)
    suspend fun like(id: Long, likeByMe: Boolean): Post

}