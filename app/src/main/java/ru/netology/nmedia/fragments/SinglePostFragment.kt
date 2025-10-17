package ru.netology.nmedia.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import getStrViewFromInt
import ru.netology.nmedia.R
import ru.netology.nmedia.fragments.FeedFragment.Companion.textArgs
import ru.netology.nmedia.databinding.FragmentSinglePostBinding
import ru.netology.nmedia.viewmodel.PostViewModel


class SinglePostFragment  : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSinglePostBinding.inflate(
            inflater,
            container,
            false,
        )


        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val postId = arguments?.textArgs ?: -1

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.posts.find { it.id.toString() == postId.toString()} ?: return@observe
            with(binding.post) {
                author.text = post.author
                content.text = post.content
                published.text = post.published
                share.text = getStrViewFromInt(post.shareCount)
                viewCount.text = post.viewCount.toString()
                like.apply{
                    isChecked = post.likedByMe
                    text = post.likes.toString()
                }
                if(post.videoUrl != null){
                    videoUrl.visibility = View.VISIBLE
                }else {
                    videoUrl.visibility = View.GONE
                }

                like.setOnClickListener {
                    viewModel.like(post.id)
                }

                share.setOnClickListener {
                    viewModel.share(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)
                }
                more.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.post_options)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removeById(post.id)
                                    findNavController().navigateUp()
                                    true
                                }
                                R.id.edit -> {
                                    viewModel.edit(post)
                                    findNavController().navigate(
                                        R.id.action_singlePostFragment_to_newPostFragment,
                                        Bundle().apply {
                                            textArgs = post.content
                                        }
                                    )
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }

            }

        }

        return binding.root
    }



}