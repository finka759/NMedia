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
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.view.load


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
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallBack) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> throw IllegalArgumentException("unknown item type")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractorListener)
            }

            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = getItem(position)){
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> throw IllegalArgumentException("unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.image.load("${BuildConfig.BASE_URL}/media/${ad.image}")
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

object PostDiffCallBack : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}