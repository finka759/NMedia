package ru.netology.nmedia.fragments

import android.os.Bundle

//import androidx.activity.enableEdgeToEdge
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat

import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractorListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostAdapter

import ru.netology.nmedia.dto.Post

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.util.StringArg


class FeedFragment : Fragment() {

    companion object{
        var Bundle.textArgs by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        enableEdgeToEdge()
        val binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false,
        )

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        val adapter = PostAdapter(object : OnInteractorListener {
            override fun onLike(post: Post) {
                viewModel.like(post.id)
            }

            override fun onShare(post: Post) {
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

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArgs = post.content
                    }
                )
                viewModel.edit(post)
            }

            override fun toSinglePost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_singlePostFragment,
                    Bundle().apply {
                        textArgs = post.id.toString()
                    }
                )
            }

            override fun onVideoPlay(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, post.videoUrl?.toUri())
                val playWebVideoIntent =
                    Intent.createChooser(intent, getString(R.string.play_web_video))
                startActivity(playWebVideoIntent)
            }

        }
        )

        binding.list.adapter = adapter
        adapter.submitList(viewModel.data)
//        viewModel.data.observe(viewLifecycleOwner) { posts ->
//            val isNew = posts.size != adapter.itemCount
//            adapter.submitList(posts) {
//                if (isNew) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//        }


        return binding.root
    }

}