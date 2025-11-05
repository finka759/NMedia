package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
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

    private val repository: PostRepository =
        PostRepositoryNetworkImpl(AppDb.getInstance(application).postDao())
    var gDraftContent: String = ""

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> =
        repository.data.asFlow()
            .combine(repository.isEmpty().asFlow(), ::FeedModel)
            .asLiveData()

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }


    fun loadPosts() {
        Log.d("MyTag", "PostViewModel.loadPosts() called")
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getAllAsync()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = FeedModelState(error = true)
            }

        }
    }

    fun like(id: Long) {

        Log.d("MyTag", "PostViewModel.like called for Post ID: $id")

        val currentState = data.value ?: return
        Log.d("MyTag", "ViewModel: currentState is not null. Posts count: ${currentState.posts.size}")

        val posts = currentState.posts
        val post = posts.find { it.id == id } ?: return
        val likedByMe = post.likedByMe


        viewModelScope.launch {
            try {

                val updatedPost = repository.like(id, likedByMe)

                val refreshState = _data.value ?: return@launch
                val updatedPosts = refreshState.posts.map {
                    if (it.id == updatedPost.id) updatedPost else it
                }
                _data.value = refreshState.copy(posts = updatedPosts)

            } catch (e: Exception) {

                _data.value = currentState
            }
        }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

//        val currentState = _data.value ?: return
//        _data.postValue(currentState.copy(posts = currentState.posts.filter { it.id != id }))
//        repository.removeById(id, object : PostRepository.PostCallback<Unit> {
//            override fun onSuccess(result: Unit) {
//            }
//            override fun onError(error: Throwable) {
//                _data.postValue(currentState)
//            }
//        })

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
        viewModelScope.launch {
            edited.value?.let {
                repository.save(it)
                _postCreated.value = Unit
            }
            edited.value = empty
        }
    }


    fun edit(post: Post) {
        edited.value = post
    }

    fun setEmtyPostToEdited() {
        edited.value = empty
    }

    fun share(id: Long){
        //TODO::
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedModelState( refreshing = true)
            try {
                repository.getAllAsync()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = FeedModelState(error = true)
            }

        }
    }

}