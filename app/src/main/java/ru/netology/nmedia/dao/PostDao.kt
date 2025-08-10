package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM Post_Entity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>


    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE Post_Entity SET content = :content WHERE id = :id")
    fun updateContentById(id: Long, content: String)

    fun save(post: PostEntity){
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)
    }

    @Query("""
        UPDATE Post_Entity SET
        likeCount = likeCount + CASE WHEN likeByMe THEN -1 ELSE 1 END,
        likeByMe = CASE WHEN likeByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    fun likeById(id: Long)

    @Query("""
        UPDATE Post_Entity SET
        shareCount = shareCount + 1
        WHERE id = :id
        """)
    fun shareById(id: Long)

    @Query("DELETE FROM Post_Entity WHERE id = :id")
    fun removeById(id: Long)

}