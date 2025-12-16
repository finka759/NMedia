package ru.netology.nmedia.auth

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val ID_KEY = "ID_KEY"
    private val TOKEN_KEY = "TOKEN_KEY"
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _data: MutableStateFlow<Token?>


    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            _data = MutableStateFlow(null)
        } else {
            _data = MutableStateFlow(Token(id, token))
        }
        sendPushToken()
    }
    val data = _data.asStateFlow()


    @Synchronized
    fun setAuth(id: Long, token: String) {
        Log.d("MyTagAuthDebug", "!!! setAuth CALLED !!! ID: $id, Token: $token")
        _data.value = Token(id, token)
        with(prefs.edit()) {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _data.value = null
        with(prefs.edit()) {
            clear()
            apply()
        }
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint{
        fun getApiService(): PostApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
//                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().sendPushToken(
                    PushToken(
                        token ?: Firebase.messaging.token.await()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}