package ru.netology.nmedia.dto

data class Post (
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likeCount: Int = 0,
    val likeByMe: Boolean = false,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val videoUrl: String? = null,
)