package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetworkImpl


private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likeCount = 0,
    likeByMe = false
)

class PostViewModel (application: Application): AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryNetworkImpl(
//        AppDb.getInstance(application).postDao()
    )
    var gDraftContent: String = ""

    val data: List<Post> = repository.get()
    private val edited = MutableLiveData(empty)
    fun like(id: Long) = repository.like(id)
    fun share(id: Long) = repository.share(id)
    fun removeById(id: Long) = repository.removeById(id)

    fun changeContent(content: String) {
        val text = content.trim()
        edited.value?.let {
            if (text == it.content) {
                return@let
            }
            edited.value = it.copy(content = text)
        }

    }

    fun save() {
        edited.value?.let {
            repository.save(it)
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun setEmtyPostToEdited() {
        edited.value =empty
    }


}