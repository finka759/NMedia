package ru.netology.nmedia.repository

import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryNetworkImpl : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}
    private val typeToken2 = object : TypeToken<Post>() {}

    companion object {
        internal const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }


//    override fun get(): List<Post> {
//        val request: Request = Request.Builder()
//            .url("${BASE_URL}/api/slow/posts")
//            .build()
//
//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let {
//                gson.fromJson(it, typeToken.type)
//            }
//    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    val posts = response.body?.string() ?: throw RuntimeException("body is null")
                    callback.onSuccess(gson.fromJson(posts, typeToken.type))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })

//        Glide.with(binding.image)
//            .load("${BASE_URL}/avatars/sber.jpg")
//            .into(binding.image)

    }


    override fun like(id: Long, likedByMe: Boolean): Post {

        val EMPTY_REQUEST = ByteArray(0).toRequestBody();


        val request = if (!likedByMe) {
            Request.Builder()
                .post(EMPTY_REQUEST)
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()
        } else {
            Request.Builder()
                .delete()
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()
        }




        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let {
                gson.fromJson(it, typeToken2.type)
            }


    }

    override fun share(id: Long) {
        TODO()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun saveAsync(post: Post, callback: PostRepository.SaveCallback) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

//        client.newCall(request)
//            .execute()
//            .close()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    val post = response.body?.string() ?: throw RuntimeException("body is null")
                    callback.onSuccess(gson.fromJson(post, typeToken2.type))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })

    }


}