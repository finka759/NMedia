package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val removeErrorPostId: Long? = null, // поле для ID поста с ошибкой удаления(для повторного удаления в случае ошибки)
    val likeError: Boolean = false
){
    /**
     * Создает новое состояние с выключенными флагами ошибок.
     */
    fun resetErrors(): FeedModelState = this.copy(
        error = false,
        removeErrorPostId = null,
        likeError = false
    )
}

