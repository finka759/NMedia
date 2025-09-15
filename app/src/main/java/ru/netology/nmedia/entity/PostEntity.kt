package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity(tableName = "Post_Entity")
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likeCount: Int = 0,
    val likeByMe: Boolean = false,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val videoUrl: String? = null,
) {
    fun toDto() = Post(
        id = id,
        author = author,
        published = published,
        content = content,
        likes = likeCount,
        likedByMe = likeByMe,
        shareCount = shareCount,
        viewCount = viewCount,
        videoUrl = videoUrl,
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                author = dto.author,
                published = dto.published,
                content = dto.content,
                likeCount = dto.likes,
                likeByMe = dto.likedByMe,
                shareCount = dto.shareCount,
                viewCount = dto.viewCount,
                videoUrl = dto.videoUrl,
            )
    }
}