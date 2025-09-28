package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun get(): List<Post>
    fun like(id: Long, likeByMe: Boolean): Post
    fun share(id: Long)
    fun removeById(id: Long)
//    fun save(post: Post)

    fun saveAsync(post: Post, callback: SaveCallback)

    interface SaveCallback{
        fun onSuccess(post: Post)
        fun onError(e: Exception)
    }

    fun getAllAsync(callback: GetAllCallback)

    interface GetAllCallback{
        fun onSuccess(posts: List<Post>)
        fun onError(e: Exception)
    }
}