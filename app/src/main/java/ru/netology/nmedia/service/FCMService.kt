package ru.netology.nmedia.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.di.DependencyContainer
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()
    private val recipientIdKey = "recipientId"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        Log.i("---fsm-token---", token)
        DependencyContainer.getInstance().appAuth.sendPushToken(token)

    }

    override fun onMessageReceived(message: RemoteMessage) {
//        Log.i("---fsm-message-geted---", message.data.toString())
        Log.d("FCMService", "Message data received: ${message.data}")
        // Получаем текущего пользователя из AppAuth
        val currentUserId = DependencyContainer.getInstance().appAuth.data.value?.id ?: 0L

        // Извлекаем строку внутреннего JSON по ключу "content"
        val innerJsonString = message.data[content] ?: run {
            Log.e("FCMService", "Outer 'content' key missing or null.")
            return
        }
        // Парсим эту строку во временный динамический Map<String, Any>
        val innerMap = try {
            // Используем Any вместо String, чтобы избежать краша на числе 10 (Double)
            gson.fromJson(innerJsonString, Map::class.java) as Map<String, Any>?
        } catch (e: Exception) {
            Log.e("FCMService", "Failed to parse inner JSON to Map", e)
            null
        } ?: return
        // Получаем значение recipientId из Map и приводим его к строке для when/Long
        // Gson парсит число 10 как Double 10.0
        val recipientIdAny = innerMap[recipientIdKey]

        // Преобразуем его безопасно в строку "10" или "null"
        val recipientIdString = when (recipientIdAny) {
            is Double -> recipientIdAny.toLong().toString()
            is String -> recipientIdAny // Если вдруг сервер прислал "10" в кавычках
            else -> null // Если recipientIdKey вообще не существует или равен null
        }


//        try {
//            message.data[action]?.let {
//                when (Action.valueOf(it)) {
//                    Action.LIKE -> handleLike(
//                        gson.fromJson(message.data[content], ActionLike::class.java)
//                    )
//
//                    Action.NEW_POST -> handleNewPost(
//                        gson.fromJson(message.data[content], NewPost::class.java)
//                    )
//                }
//            }
//        } catch (e: IllegalArgumentException) {
//            Log.i("---fsm-message-geted---", message.data.toString())
//            println(e.message)
//            for (line in e.stackTrace) {
//                println("at $line")
//                Log.i("---error---", "at $line")
//            }
//        }

//        Log.i("---fsm-message---", message.notification?.body.toString())

        when (recipientIdString) {
            null -> {
                Log.d("FCMService", "RecipientId is null (mass broadcast).")
                handleActionMessageFromMap(innerMap)
            }

            "0" -> {
                if (currentUserId != 0L) {
                    Log.d(
                        "FCMService",
                        "Server thinks anonymous (0), but we are logged in. Resending token."
                    )
                    DependencyContainer.getInstance().appAuth.sendPushToken()
                } else {
                    handleActionMessageFromMap(innerMap)
                }
            }

            else -> {
                val recipientId = recipientIdString.toLongOrNull()
                if (recipientId != null && recipientId == currentUserId) {
                    Log.d("FCMService", "RecipientId matches current user ID.")
                    handleActionMessageFromMap(innerMap)
                } else {
                    Log.d("FCMService", "RecipientId mismatch. Resending token.")
                    DependencyContainer.getInstance().appAuth.sendPushToken()
                }
            }
        }
    }

    // --- Новый вспомогательный метод для обработки из Map ---
    private fun handleActionMessageFromMap(dataMap: Map<String, Any>) {
        try {
            val actionString = dataMap[action] as? String
            val contentString = dataMap[content] as? String

            if (contentString != null) {
                val actionType = actionString?.let { Action.valueOf(it) } ?: Action.UNIVERSAL
                when (actionType) {
                    Action.LIKE -> handleLike(
                        gson.fromJson(contentString, ActionLike::class.java)
                    )
                    Action.NEW_POST -> handleNewPost(
                        gson.fromJson(contentString, NewPost::class.java)
                    )
                    Action.UNIVERSAL -> handleUniversal(contentString)
                }
            } else {
                Log.d("FCMService", "Action or Content missing in inner map.")
            }
        } catch (e: Exception) {
            Log.e("---error---", "Error in handleActionMessageFromMap.", e)
        }
    }


    private fun handleUniversal(contentText: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText) // Используем переданную строку contentText
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
        Log.d("FCMService", "Showing universal notification.")
    }


    private fun handleLike(content: ActionLike) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
    }


    private fun handleNewPost(content: NewPost) {

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_new_post,
                    content.postAuthor
                )
            )
            .setContentText(content.postTopic)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content.postText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }


}


enum class Action {
    LIKE, NEW_POST, UNIVERSAL
}

data class ActionLike(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

data class NewPost(
    val postAuthor: String,
    val postText: String,
    val postTopic: String
)


