package ru.netology.nmedia.dto

data class Post (
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    var likeCount: Int = 5,
    val likeByMe: Boolean = false,
    val shareCount: Int = 10,
    var viewCount: Int = 0,
)