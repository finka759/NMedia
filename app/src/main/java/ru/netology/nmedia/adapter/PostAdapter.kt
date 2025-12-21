package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import getStrViewFromInt
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType


interface OnInteractorListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onVideoPlay(post: Post)
    fun toSinglePost(post: Post)

    fun onImageClick(imageUrl: String)

}

class PostAdapter(
    private val onInteractorListener: OnInteractorListener
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallBack) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractorListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }


}


class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractorListener: OnInteractorListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) = with(binding) {
        author.text = post.author
        content.text = post.content
        published.text = post.published
        share.text = getStrViewFromInt(post.shareCount)
        viewCount.text = post.viewCount.toString()

        like.apply {
            isChecked = post.likedByMe
            text = post.likes.toString()
        }
        if (post.videoUrl != null) {
            videoUrl.visibility = View.VISIBLE
        } else {
            videoUrl.visibility = View.GONE
        }

        like.setOnClickListener {
            onInteractorListener.onLike(post)

        }
        share.setOnClickListener {
            onInteractorListener.onShare(post)

        }


        val urlPhoto = "${BuildConfig.BASE_URL}/media/${post.attachment?.url}"
//        val url = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
        // --- Вот место для отображения изображения поста ---
        if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) {
            // Если вложение есть и это изображение:
            postImage.visibility = View.VISIBLE
            // Используем Glide для загрузки изображения по URL из интернета
            Glide.with(postImage)
                .load(urlPhoto) // URL изображения из DTO Post
                .placeholder(R.drawable.ic_loading_100dp) // Заглушка во время загрузки
                .error(R.drawable.ic_error_100dp) // Изображение ошибки при неудаче
                .timeout(10_000)
                .into(postImage)

            // !!! НОВЫЙ OnClickListener для изображения вызывает onImageClick через интерфейс !!!
            postImage.setOnClickListener {
                onInteractorListener.onImageClick(urlPhoto)
            }

        } else {
            // Если вложения нет или оно другого типа, скрываем ImageView
            postImage.visibility = View.GONE
            postImage.setOnClickListener(null)
        }

        more.isVisible = post.ownedByMe

        more.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.post_options)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onInteractorListener.onRemove(post)
                            true
                        }

                        R.id.edit -> {
                            onInteractorListener.onEdit(post)
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }
        videoUrl.setOnClickListener {
            onInteractorListener.onVideoPlay(post)
        }
        content.setOnClickListener {
            onInteractorListener.toSinglePost(post)
        }
        avatar.setOnClickListener {
            onInteractorListener.toSinglePost(post)
        }
        author.setOnClickListener {
            onInteractorListener.toSinglePost(post)
        }
        published.setOnClickListener {
            onInteractorListener.toSinglePost(post)
        }
        barrier.setOnClickListener {
            onInteractorListener.toSinglePost(post)
        }


        val url = "${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}"
//        val url = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
        Glide.with(avatar)
            .load(url)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .circleCrop()
            .into(avatar)

    }
}

object PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

}