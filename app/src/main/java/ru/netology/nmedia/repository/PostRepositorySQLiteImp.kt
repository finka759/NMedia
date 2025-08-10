//package ru.netology.nmedia.repository
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import ru.netology.nmedia.dao.PostDao
//import ru.netology.nmedia.dto.Post
//
//class PostRepositorySQLiteImpl(
//    private val dao: PostDao
//) : PostRepository {
//    private var posts = emptyList<Post>()
//    private val data = MutableLiveData(posts)
//
//    init {
//        posts = dao.getAll()
//        data.value = posts
//    }
//
//    override fun share(id: Long) {
//        dao.shareById(id)
//        posts = posts.map { post ->
//            if (post.id == id) {
//                post.copy(
//                    shareCount = post.shareCount + 1
//                )
//            } else {
//                post
//            }
//        }
//        data.value = posts
//    }
//
//
//    override fun get(): LiveData<List<Post>> = data
//
//
//    override fun save(post: Post) {
//        val id = post.id
//        val saved = dao.save(post)
//        posts = if (id == 0L) {
//            listOf(saved) + posts
//        } else {
//            posts.map {
//                if (it.id != id) it else saved
//            }
//        }
//        data.value = posts
//    }
//
//    override fun like(id: Long) {
//        dao.likeById(id)
//        posts = posts.map {
//            if (it.id != id) it else it.copy(
//                likeByMe = !it.likeByMe,
//                likeCount = if (it.likeByMe) it.likeCount - 1 else it.likeCount + 1
//            )
//        }
//        data.value = posts
//    }
//
//    override fun removeById(id: Long) {
//        dao.removeById(id)
//        posts = posts.filter { it.id != id }
//        data.value = posts
//    }
//}