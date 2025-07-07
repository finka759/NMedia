package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractorListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.focusAndShowKeyboard
import android.content.Intent
import androidx.activity.result.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
//        binding.groupForEdit.visibility = View.GONE
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val viewModel: PostViewModel by viewModels()
        val newPostLauncher = registerForActivityResult(NewPostResultContract) { result ->
            result ?: return@registerForActivityResult
            viewModel.changeContent(result)
            viewModel.save()
        }
        binding.fab.setOnClickListener {
            newPostLauncher.launch(null)
        }
        val adapter = PostAdapter(object : OnInteractorListener {
            override fun onLike(post: Post) {
                viewModel.like(post.id)
            }

            override fun onShare(post: Post) {
//                viewModel.share(post.id)

//                val intent = Intent().apply{
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_TEXT, post.content)
//                    type = "text/plain"
//                }
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
//                startActivity(intent)
                startActivity(intent)

            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                newPostLauncher.launch(post.content)

            }

        }
        )

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            val isNew = posts.size != adapter.itemCount
            adapter.submitList(posts) {
                if (isNew) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }








//        viewModel.edited.observe(this) { post ->
//            if (post.id != 0L) {
//                binding.groupForEdit.visibility = View.VISIBLE
//                binding.editMessageTextContent.text = post.content
//                with(binding.content) {
//                    requestFocus()
////                    focusAndShowKeyboard()
//                    setText(post.content)
//                }
//
//
//            }
//        }
//
//        with(binding) {
//            save.setOnClickListener {
//                if (content.text.isNullOrBlank()) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        R.string.error_empty_content,
//                        Toast.LENGTH_LONG
//                    ).show()
//                    return@setOnClickListener
//                }
//                viewModel.changeContent(content.text.toString())
//                viewModel.save()
//                content.setText("")
//                content.clearFocus()
//                binding.groupForEdit.visibility = View.GONE
//                AndroidUtils.hideKeyboard(it)
//            }
//            closeEditButton.setOnClickListener {
//                viewModel.setEmtyPostToEdited()
//                content.setText("")
//                content.clearFocus()
//                binding.groupForEdit.visibility = View.GONE
//                AndroidUtils.hideKeyboard(it)
//            }
//        }
    }
}

