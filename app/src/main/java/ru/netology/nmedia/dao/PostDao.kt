package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM Post_Entity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM Post_Entity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    // запрос, чтобы выбирать только видимые посты, отсортированные по ID убыванию
    @Query("SELECT * FROM Post_Entity WHERE isVisible = 1 ORDER BY id DESC")
    fun getAllVisible(): Flow<List<PostEntity>>

    // Добавляем метод для пометки всех невидимых постов как видимых
    @Query("UPDATE Post_Entity SET isVisible = 1 WHERE isVisible = 0")
    suspend fun showAllInvisible()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE Post_Entity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    @Query("""
        UPDATE Post_Entity SET
        likeCount = likeCount + CASE WHEN likeByMe THEN -1 ELSE 1 END,
        likeByMe = CASE WHEN likeByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    suspend fun likeById(id: Long)

    @Query("""
        UPDATE Post_Entity SET
        shareCount = shareCount + 1
        WHERE id = :id
        """)
    suspend fun shareById(id: Long)

    @Query("DELETE FROM Post_Entity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT COUNT(*) = 0 FROM Post_Entity")
    fun isEmpty(): LiveData<Boolean>
    @Query("SELECT * FROM Post_Entity WHERE id = :id")
    suspend fun getPostById(id: Long): PostEntity?

    @Query("DELETE FROM Post_Entity")
    suspend fun clear()

}