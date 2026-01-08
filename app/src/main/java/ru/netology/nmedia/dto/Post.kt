package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

sealed interface FeedItem{
    val id: Long
}


data class Post(
    override val id: Long,
    val author: String,
    val authorId: Long,
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
    val ownedByMe: Boolean = false,
): FeedItem

data class Ad(
    override val id: Long,
    val image: String,
): FeedItem


data class Attachment(
    val url: String,
    val type: AttachmentType,
)