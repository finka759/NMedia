package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>>
    fun isEmpty(): LiveData<Boolean>
    suspend fun getAllAsync()
    fun getNewerCount(id: Long): Flow<Int>
    // Добавляем новый метод для пометки всех невидимых постов как видимых
    suspend fun showAllInvisible()
    suspend fun save(post: Post): Post
    suspend fun saveWithAttachment(post: Post, photo: File?)
    suspend fun removeById(id: Long)
    suspend fun like(id: Long, likeByMe: Boolean): Post

}