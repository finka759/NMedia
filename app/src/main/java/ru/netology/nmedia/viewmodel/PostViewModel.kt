package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetworkImpl
import ru.netology.nmedia.util.call


private val empty = Post(
    id = 0,
    author = "",
    published = "",
    content = "",
    isVisible = true // По умолчанию в DTO пусть будет true
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository =
        PostRepositoryNetworkImpl(AppDb.getInstance(application).postDao())
    var gDraftContent: String = ""

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state


    //    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> =
        repository.data
            .combine(repository.isEmpty().asFlow(), ::FeedModel)
            .asLiveData()


//    val data: LiveData<FeedModel> = liveData {
//        // Внутри liveData builder мы можем собирать (collect) Flow
//        repository.data.collect { posts ->
//            // И можем получить текущее значение из LiveData с помощью getValue()
//            val isEmpty = repository.isEmpty().value ?: false
//
//            // Затем мы эмитим новое значение в нашу итоговую LiveData
//            emit(FeedModel(posts, isEmpty))
//        }
//    }


//    val data: LiveData<FeedModel> =
//        repository.data.map { list: List<Post> -> FeedModel(list, list.isEmpty()) }
//            .catch { it.printStackTrace() }
//            .asLiveData(Dispatchers.Default)


    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }


    /**
     * Функция, вызываемая при нажатии на баннер "Новые посты".
     * Отмечает все невидимые посты как видимые и запускает скролл.
     */
    fun showNewPosts() {
        viewModelScope.launch {
            try {
                repository.showAllInvisible() // Обновляем БД: isVisible = true для всех новых
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }


    fun loadPosts() {
        Log.d("MyTag", "PostViewModel.loadPosts() called")
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getAllAsync()
                repository.showAllInvisible()
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
        Log.d(
            "MyTag",
            "ViewModel: currentState is not null. Posts count: ${currentState.posts.size}"
        )

        val posts = currentState.posts
        val post = posts.find { it.id == id } ?: return
        val likedByMe = post.likedByMe


        viewModelScope.launch {
            try {

                repository.like(id, likedByMe)

            } catch (e: Exception) {
                _state.value = FeedModelState(error = true, likeError = true)
            }
        }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true, removeErrorPostId = id)
            }
        }
    }

    fun retryRemoveById(id: Long?) {
        if (id == null) return
        _state.value = FeedModelState(loading = true) // показать прогресс
        removeById(id) // повторяем попытку удаления
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
                try {
                    repository.save(it)
                    _postCreated.value = Unit
                    gDraftContent = ""
                } catch (e: Exception) {
                    gDraftContent = it.content
                    _state.value = FeedModelState(error = true)
                }
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

    fun share(id: Long) {
        //TODO::
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)
            try {
                repository.getAllAsync()
                repository.showAllInvisible()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = FeedModelState(error = true)
            }

        }
    }

    fun resetErrorState() {
        // Берем текущее значение _state, вызываем метод сброса ошибок,
        // и присваиваем новое состояние обратно в _state.
        _state.value = _state.value?.resetErrors() ?: FeedModelState()
    }

}