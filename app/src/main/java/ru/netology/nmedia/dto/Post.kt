package ru.netology.nmedia.dto

data class Post (
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    var likeCount: Int = 0,
    var likeByMe: Boolean = false,
    var shareCount: Int = 0,
    var viewCount: Int = 0,
)