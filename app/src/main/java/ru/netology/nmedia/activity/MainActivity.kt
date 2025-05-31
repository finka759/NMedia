package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import getStrViewFromInt
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post


class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
        setContentView(binding.root)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                content.text = post.content
                published.text = post.published
                likeCount.text = getStrViewFromInt(post.likeCount)
                shareCount.text = getStrViewFromInt(post.shareCount)
                viewCount.text = post.viewCount.toString()
//                if (post.likeByMe) {
//                    favoriteBorder.setImageResource(R.drawable.baseline_favorite_24)
//                }
                favoriteBorder.setImageResource(
                    if (post.likeByMe) {
                        post.likeCount += 1
                        likeCount.text = getStrViewFromInt(post.likeCount)
                        R.drawable.baseline_favorite_24
                    } else {
                        post.likeCount -= 1
                        likeCount.text = getStrViewFromInt(post.likeCount)
                        R.drawable.baseline_favorite_border_24
                    }
                )
            }

            binding.favoriteBorder.setOnClickListener {
                viewModel.like()
//                post.likeByMe = !post.likeByMe
//
//                favoriteBorder.setImageResource(
//                    if (post.likeByMe) {
//                        post.likeCount += 1
//                        likeCount.text = getStrViewFromInt(post.likeCount)
//                        R.drawable.baseline_favorite_24
//                    } else {
//                        post.likeCount -= 1
//                        likeCount.text = getStrViewFromInt(post.likeCount)
//                        R.drawable.baseline_favorite_border_24
//                    }
//                )
            }
//            binding.share.setOnClickListener {
//                post.shareCount += 1
//                shareCount.text = getStrViewFromInt(post.shareCount)
//            }
        }
    }
}

