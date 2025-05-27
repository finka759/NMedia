package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import java.math.RoundingMode


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likeCount = 1125,
            likeByMe = false,
            shareCount = 6999,
            viewCount = 5,
        )
        with(binding) {
            author.text = post.author
            content.text = post.content
            published.text = post.published
            likeCount.text = getStrViewFromInt(post.likeCount)
            shareCount.text = getStrViewFromInt(post.shareCount)
            viewCount.text = post.viewCount.toString()
            if (post.likeByMe) {
                favoriteBorder.setImageResource(R.drawable.baseline_favorite_24)
            }

            favoriteBorder.setOnClickListener {
                post.likeByMe = !post.likeByMe

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
            share.setOnClickListener {
                post.shareCount += 1
                shareCount.text = getStrViewFromInt(post.shareCount)
            }
        }
    }
}

fun getStrViewFromInt(i: Int): String {
    return if (i < 1000) {
        i.toString()
    } else if (i < 10000) {
        "%.1f K".format(i / 100 / 10.0, RoundingMode.DOWN)
    } else if (i < 1_000_000) {
        (i / 1000).toString() + "K"
    } else {
        "%.1f M".format(i / 100000 / 10.0, RoundingMode.DOWN)
    }
}