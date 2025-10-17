package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun like(id: Long, likedByMe: Boolean, callback: PostCallback<Post>)
    fun share(id: Long)
    fun removeById(id: Long, callback: PostCallback<Unit>)
    fun save(post: Post,  callback: PostCallback<Post>)
    fun getAllAsync(callback: PostCallback<List<Post>>)

    interface PostCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: Throwable)
    }
}