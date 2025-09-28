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
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

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
    }

    override fun onMessageReceived(message: RemoteMessage) {
//        Log.i("---fsm-message-geted---", message.data.toString())
        try {
            message.data[action]?.let {
                when (Action.valueOf(it)) {
                    Action.LIKE -> handleLike(
                        gson.fromJson(message.data[content], ActionLike::class.java)
                    )
                    Action.NEW_POST -> handleNewPost(
                        gson.fromJson(message.data[content], NewPost::class.java)
                    )
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.i("---fsm-message-geted---", message.data.toString())
            println(e.message)
            for (line in e.stackTrace) {
                println("at $line")
                Log.i("---error---", "at $line")
            }
        }

//        Log.i("---fsm-message---", message.notification?.body.toString())
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
    LIKE, NEW_POST,
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

