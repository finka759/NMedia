package ru.netology.nmedia.auth

import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.Api
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token

class AppAuth private constructor(context: Context) {
    companion object{
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"
        private var INSTANCE: AppAuth? = null

        fun init(context: Context){
            INSTANCE = AppAuth(context)
        }

        fun getInstance() = requireNotNull(INSTANCE){
            "Need call init() first!"
        }
    }
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _data: MutableStateFlow<Token?>


    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            _data = MutableStateFlow(null)
//            with(prefs.edit()) {
//                clear()
//                apply()
//            }
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

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                PostApi.service.sendPushToken(PushToken(token?: Firebase.messaging.token.await()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}