package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetworkImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likes = 0,
    likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryNetworkImpl()
    var gDraftContent: String = ""

    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {

        _data.postValue(FeedModel(loading = true))

        repository.getAllAsync(
            object : PostRepository.PostCallback<List<Post>> {

                override fun onSuccess(result: List<Post>) {
                    _data.value = (FeedModel(posts = result, empty = result.isEmpty()))
                }

                override fun onError(error: Throwable) {
                    _data.value = (FeedModel(error = true))
                }

            })

    }

    fun like(id: Long) {

        val currentState = _data.value ?: return
        val posts = currentState.posts
        val post = posts.find { it.id == id } ?: return
        val likedByMe = post.likedByMe

        repository.like(id, likedByMe, object : PostRepository.PostCallback<Post> {

            override fun onSuccess(result: Post) {
                val refreshState = _data.value ?: return
                val updatedPosts = refreshState.posts.map {
                    if (it.id == result.id) result else it
                }
                _data.postValue(refreshState.copy(posts = updatedPosts))
            }

            override fun onError(error: Throwable) {
                _data.value
            }

        })
    }

    fun share(id: Long) = repository.share(id)

    fun removeById(id: Long) {

        val currentState = _data.value ?: return
        _data.postValue(currentState.copy(posts = currentState.posts.filter { it.id != id }))

        repository.removeById(id, object : PostRepository.PostCallback<Unit> {

            override fun onSuccess(result: Unit) {
            }

            override fun onError(error: Throwable) {
                _data.postValue(currentState)
            }

        })

    }


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
            repository.save(it, object : PostRepository.PostCallback<Post> {

                override fun onSuccess(result: Post) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Throwable) {
                    _data.value
                }
            })
        }
        edited.value = empty
    }


    fun edit(post: Post) {
        edited.value = post
    }

    fun setEmtyPostToEdited() {
        edited.value = empty
    }

}