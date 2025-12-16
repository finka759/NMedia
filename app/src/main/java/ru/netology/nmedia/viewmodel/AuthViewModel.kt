package ru.netology.nmedia.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
//import ru.netology.nmedia.api.AuthService
//import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
//    private val authService: AuthService,
    private val apiService: PostApiService,
    ) : ViewModel() {
    val data = appAuth
        .data
        .asLiveData(Dispatchers.Default)

    val isAuthorized: Boolean
        get() = appAuth.data.value?.id != null


    // Событие для оповещения фрагмента, что вход успешен и можно возвращаться назад
    private val _signInEvent = SingleLiveEvent<Unit>()
    val signInEvent: SingleLiveEvent<Unit>
        get() = _signInEvent

    // Состояние UI для отображения загрузки или ошибок
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
    }


    fun signIn(login: String, pass: String) {
        _uiState.value = UiState.Loading // Начинаем загрузку
        viewModelScope.launch {
            try {
                // Отправляем сетевой запрос через глобальный Api.authService
                val response = apiService.signIn(login, pass)

                if (response.isSuccessful) {
                    val body = response.body() ?: throw RuntimeException("Тело ответа пустое")

                    // Сохраняем полученные данные (id и token) через синглтон AppAuth
                    appAuth.setAuth(body.id, body.token)

                    _uiState.value = UiState.Idle // Завершаем загрузку
                    _signInEvent.value =
                        Unit// Генерируем событие для фрагмента: "Успех, можно закрываться"

                } else {
                    _uiState.value = UiState.Error("Ошибка сервера: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Сетевая ошибка: ${e.message}")
            }
        }
    }

    // метод logout для полноты
    fun logout() {
        appAuth.removeAuth()
    }


    // Событие для навигации на экран аутентификации из любого места (например, из PostViewModel)
    private val _navigateToSignInEvent = SingleLiveEvent<Unit>()
    val navigateToSignInEvent: SingleLiveEvent<Unit>
        get() = _navigateToSignInEvent

    fun navigateToSignIn() {
        _navigateToSignInEvent.value = Unit
    }


}