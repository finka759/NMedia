package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import getStrViewFromInt
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

//typealias OnLikeListener = (post: Post) -> Unit
//typealias OnShareListener = (post: Post) -> Unit
//typealias OnRemoveListener = (post: Post) -> Unit

interface OnInteractorListener {
    fun OnLike(post: Post)
    fun OnShare(post: Post)
    fun OnRemove(post: Post)
    fun OnEdit(post: Post)
}

class PostAdapter(
//    private val onLikeListener: OnLikeListener,
//    private val onShareListener: OnShareListener,
//    private val onRemoveListener: OnRemoveListener
    private val onInteractorListener: OnInteractorListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractorListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
//    private val onLikeListener: OnLikeListener,
//    private val onShareListener: OnShareListener,
//    private val onRemoveListener: OnRemoveListener
    private val onInteractorListener: OnInteractorListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) = with(binding) {
        author.text = post.author
        content.text = post.content
        published.text = post.published
        likeCount.text = getStrViewFromInt(post.likeCount)
        shareCount.text = getStrViewFromInt(post.shareCount)
        viewCount.text = post.viewCount.toString()
        favoriteBorder.setImageResource(
            if (post.likeByMe) {
                R.drawable.baseline_favorite_24
            } else {
                R.drawable.baseline_favorite_border_24
            }
        )
        favoriteBorder.setOnClickListener {
            onInteractorListener.OnLike(post)
//            onLikeListener(post)
        }
        share.setOnClickListener {
            onInteractorListener.OnShare(post)
//            onShareListener(post)
        }
        more.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.post_options)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onInteractorListener.OnRemove(post)
//                            onRemoveListener(post)
                            true
                        }
                        R.id.remove -> {
                            onInteractorListener.OnEdit(post)
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }
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