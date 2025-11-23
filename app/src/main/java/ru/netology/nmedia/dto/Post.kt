package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val videoUrl: String? = null,
    var authorAvatar: String? = null,
    val isVisible: Boolean = false,
    val attachment: Attachment? = null,
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)