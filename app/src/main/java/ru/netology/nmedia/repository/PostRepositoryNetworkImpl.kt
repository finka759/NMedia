package ru.netology.nmedia.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Transaction
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity


class PostRepositoryNetworkImpl(
    private val dao: PostDao
) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map{
        it.map (PostEntity::toDto)
    }

    override fun isEmpty() = dao.isEmpty()


//    override suspend fun getAllAsync() {
//        Log.d("MyTag", "Repository.getAllAsync(): STARTING NETWORK REQUEST") // Лог 1
//        val posts = PostApi.service.getAll()
//        Log.d("MyTag", "Repository.getAllAsync(): NETWORK SUCCESS. Received ${posts.size} posts.") // Лог 2
//        dao.insert(posts.map(PostEntity::fromDto))
//        Log.d("MyTag", "Repository.getAllAsync(): FINISHED.") // Лог 7
//    }


    override suspend fun getAllAsync() {
        Log.d("MyTag", "Repository.getAllAsync(): STARTING NETWORK REQUEST")
        try {
            val posts = PostApi.service.getAll()
            Log.d("MyTag", "Repository.getAllAsync(): NETWORK SUCCESS. Received ${posts.size} posts.")
            if (posts.isNotEmpty()) { // проверка, чтобы не вызывать insert для пустого списка
                dao.insert(posts.map(PostEntity::fromDto))
                Log.d("MyTag", "Repository.getAllAsync(): INSERTED ${posts.size} posts into DB.")
            } else {
                Log.d("MyTag", "Repository.getAllAsync(): API returned 0 posts. Nothing to insert.")
            }
        } catch (e: Exception) {
            // обработка любых исключений
            Log.e("MyTag", "Repository.getAllAsync(): AN EXCEPTION OCCURRED during network request or DB operation!", e)
            // Важно: перебросить исключение, чтобы ViewModel мог его обработать
            throw e
        }
        Log.d("MyTag", "Repository.getAllAsync(): FINISHED.")
    }


//    override suspend fun save(post: Post): Post {
//        val postFromServer = PostApi.service.save(post)
//        dao.insert(PostEntity.fromDto(postFromServer))
//        return postFromServer
//    }


    override suspend fun save(post: Post): Post {
        return if (post.id == 0L) {
            val newPost = PostApi.service.save(post)
            dao.insert(PostEntity.fromDto(newPost))
            newPost
        } else {
            dao.updateContentById(post.id, post.content)
            post
        }
    }


    @Transaction
    override suspend fun removeById(id: Long) {
        try {
            PostApi.service.removeById(id)
            dao.removeById(id) // Удаляем из БД только после успешного ответа от API
        } catch (e: Exception) {
            // В случае ошибки ничего не делаем, так как Room откатит транзакцию
            throw e
        }
    }

    override suspend fun like(
        id: Long,
        likedByMe: Boolean
    ): Post {

        Log.d("MyTag", "Repository.like called for Post ID: $id, currently liked by me: $likedByMe")

        val updatedPost = if (likedByMe) {
            PostApi.service.dislikeById(id)
        } else {
            PostApi.service.likeById(id)
        }
        dao.insert(PostEntity.fromDto(updatedPost))

        return  updatedPost

    }

}