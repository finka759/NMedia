package ru.netology.nmedia.repository

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositoryFileImpl(private val context: Context) : PostRepository {

    //    private val prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    private var nextId = 1L
    private var posts = emptyList<Post>()
        set(value) {
            field = value
            data.value = posts
            sync()
        }

    private val data = MutableLiveData(posts)

//    init {
//        prefs.getString(KEY_POSTS, null)?.let { value ->
//            posts = gson.fromJson(value, type)
//            nextId = (posts.maxOfOrNull { it.id } ?: 0) + 1
////            data.value = posts
//        }
//    }

    init {
        val file = context.filesDir.resolve(FILE_NAME)
        if (file.exists()) {
            context.openFileInput(FILE_NAME).bufferedReader().use {
                posts = gson.fromJson(it, type)
                nextId = (posts.maxOfOrNull { it.id } ?: 0) + 1
//                data.value = posts
            }
        } else {
            sync()
        }
    }

    override fun get(): LiveData<List<Post>> = data

    override fun like(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(
                    likeByMe = !post.likeByMe,
                    likeCount = if (post.likeByMe) post.likeCount - 1 else post.likeCount + 1
                )
            } else {
                post
            }
        }
//        data.value = posts
    }

    override fun share(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(
                    shareCount = post.shareCount + 1
                )
            } else {
                post
            }
        }
//        data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
//        data.value = posts
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    likeByMe = false,
                    published = "now"
                )
            ) + posts
        } else {
            posts.map {
                if (post.id == it.id) {
                    it.copy(content = post.content)
                } else it
            }
        }
//        data.value = posts
    }

    private fun sync() {
//        prefs.edit {
//            putString(KEY_POSTS, gson.toJson(posts))
//        }
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }

    companion object {
        private const val FILE_NAME = "posts.json"
        private val gson = Gson()
        private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    }


}