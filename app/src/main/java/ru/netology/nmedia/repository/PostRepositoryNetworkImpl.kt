package ru.netology.nmedia.repository
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.netology.nmedia.api.PostsApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryNetworkImpl : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    //    private val gson = Gson()
//    private val typeToken = object : TypeToken<List<Post>>() {}
//    private val typeToken2 = object : TypeToken<Post>() {}
//
//    companion object {
//        internal const val BASE_URL = "http://10.0.2.2:9999"
//        private val jsonType = "application/json".toMediaType()
//    }


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

        PostsApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(
                            RuntimeException(response.errorBody()?.string())
                        )
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


    override fun like(id: Long, likedByMe: Boolean){

        val request = if (!likedByMe) {
            PostsApi.service.dislikeById(id)
        } else {
            PostsApi.service.likeById(id)
        }

//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let {
//                gson.fromJson(it, typeToken2.type)
//            }


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

    override fun save(post: Post, callback: PostRepository.SaveCallback) {
   PostsApi.service.save(post)
            .execute()

    }


}