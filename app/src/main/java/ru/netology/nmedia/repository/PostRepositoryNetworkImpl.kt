package ru.netology.nmedia.repository

import ru.netology.nmedia.api.PostApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.dto.Post


class PostRepositoryNetworkImpl : PostRepository {

    override fun getAllAsync(callback: PostRepository.PostCallback<List<Post>>) {

        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (!response.isSuccessful) {
                        when (response.code()) {
                            404 -> callback.onError(RuntimeException("Post not found"))
                            500 -> callback.onError(RuntimeException("Server error"))
                            else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                        }
                        return
                    }
                    val posts = response.body()
                    if (posts == null) {
                        callback.onError(
                            RuntimeException("Body is null")
                        )
                        return
                    }
                    callback.onSuccess(posts)
                }

                override fun onFailure(
                    call: retrofit2.Call<List<Post>>,
                    t: Throwable
                ) {
                    callback.onError(t)
                }

            })
    }

    override fun like(
        id: Long,
        likedByMe: Boolean,
        callback: PostRepository.PostCallback<Post>
    ) {

        val request = if (likedByMe) {
            PostApi.service.dislikeById(id)
        } else {
            PostApi.service.likeById(id)
        }

        request.enqueue(object : Callback<Post> {

            override fun onResponse(
                call: Call<Post>,
                response: Response<Post>
            ) {
                if (!response.isSuccessful) {
                    when (response.code()) {
                        404 -> callback.onError(RuntimeException("Post not found"))
                        500 -> callback.onError(RuntimeException("Server error"))
                        else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                    }
                    return
                }
            }

            override fun onFailure(
                call: Call<Post>,
                e: Throwable
            ) {
                callback.onError(e)
            }

        })

    }


    override fun share(id: Long) {
        TODO()
    }

    override fun removeById(id: Long, callback: PostRepository.PostCallback<Unit>) {
        PostApi.service.removeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        when (response.code()) {
                            404 -> callback.onError(RuntimeException("Post not found"))
                            500 -> callback.onError(RuntimeException("Server error"))
                            else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                        }
                        return
                    }
                    callback.onSuccess(Unit)
                }

                override fun onFailure(call: Call<Unit>, e: Throwable) {
                    callback.onError(e)
                }
            })
    }

    override fun save(post: Post, callback: PostRepository.PostCallback<Post>) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        when (response.code()) {
                            404 -> callback.onError(RuntimeException("Post not found"))
                            500 -> callback.onError(RuntimeException("Server error"))
                            else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                        }
                        return
                    }

                    val body = response.body()
                    if (body == null) {
                        callback.onError(RuntimeException("body is null"))
                    } else {
                        callback.onSuccess(body)
                    }
                }

                override fun onFailure(call: Call<Post>, e: Throwable) {
                    callback.onError(e)
                }
            })

    }

}