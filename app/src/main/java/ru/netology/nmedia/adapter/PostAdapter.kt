package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import getStrViewFromInt
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post


interface OnInteractorListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onVideoPlay(post: Post)
    fun toSinglePost(post: Post)

}

class PostAdapter(
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
//        videoUrl.visibility = View.VISIBLE

        like.setOnClickListener {
            onInteractorListener.onLike(post)

        }
        share.setOnClickListener {
            onInteractorListener.onShare(post)

        }
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