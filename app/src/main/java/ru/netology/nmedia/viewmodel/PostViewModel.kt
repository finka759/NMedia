package ru.netology.nmedia.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.repository.PostRepository
import java.io.File

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0,
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = "",
    isVisible = true // По умолчанию в DTO пусть будет true
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModel @Inject constructor(
private val repository: PostRepository,
private val appAuth: AppAuth,

) : ViewModel() {

//    private val repository: PostRepository =
//        PostRepositoryNetworkImpl(AppDb.getInstance(application).postDao())
    var gDraftContent: String = ""

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state


    //    private val _data = MutableLiveData(FeedModel())

    // Событие, которое оповестит FeedFragment, что пользователь должен войти в систему
    private val _authRequiredEvent = SingleLiveEvent<Unit>()
    val authRequiredEvent: LiveData<Unit>
        get() = _authRequiredEvent



    val data: LiveData<FeedModel> = appAuth.data.flatMapLatest { token ->
        Log.d("MyTag1", "Current User ID from token: ${token?.id}")

        repository.data
            .map { posts ->
                posts.map { post ->
                    val isOwned = post.authorId == token?.id
                    Log.d("MyTag11", "Post ID: ${post.id}, Author ID: ${post.authorId}, OwnedByMe calculated as: $isOwned")
                    post.copy(ownedByMe = isOwned)
                }

            }
            .combine(repository.isEmpty().asFlow(), ::FeedModel)
    }
        .asLiveData()


    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


    private val _photo = MutableLiveData<PhotoModel?>()
    val photo: LiveData<PhotoModel?>
        get() = _photo


    init {
        loadPosts()

        val authToken = appAuth.data.value
        Log.d("MyTag111AuthStatus", "Token available: ${authToken != null}, User ID: ${authToken?.id}")
    }

    fun updatePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun clearPhoto() {
        _photo.value = null
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


        // --- ДОБАВЛЕННАЯ ПРОВЕРКА АУТЕНТИФИКАЦИИ ---
        val isAuthorized = appAuth.data.value?.id != null
        if (!isAuthorized) {
            // Если пользователь не авторизован, генерируем событие для фрагмента
            _authRequiredEvent.value = Unit
            return // Прерываем выполнение метода like
        }
        // -------------------------------------------

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
                    repository.saveWithAttachment(it, _photo.value?.file)
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