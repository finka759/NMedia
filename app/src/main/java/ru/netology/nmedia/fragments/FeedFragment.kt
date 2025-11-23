package ru.netology.nmedia.fragments

import android.os.Bundle

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.util.StringArg


class FeedFragment : Fragment() {

    companion object {
        var Bundle.textArgs by StringArg
    }

    // Флаг для отслеживания необходимости скролла после обновления данных
    private var shouldScrollToTop = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false,
        )

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


            override fun onImageClick(imageUrl: String) {
                // Создаем и показываем PhotoPostFragment
                val photoPostFragment = PhotoPostFragment().newInstance(imageUrl)
                // Используем childFragmentManager, так как мы во фрагменте
                // R.id.fragment_container_for_fullscreen - это ID FrameLayout в разметке FeedFragment
                childFragmentManager.beginTransaction()
                    .add(R.id.fragment_container_for_fullscreen, photoPostFragment) // Используем .add, чтобы PhotoPostFragment наложился поверх
                    .addToBackStack(null) // Это позволит вернуться к FeedFragment по кнопке "Назад"
                    .commit()
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

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val listWasEmpty = adapter.itemCount == 0
            adapter.submitList(state.posts){
                // Callback submitList выполняется после того, как список отрисован
                // Если список был пуст ИЛИ если флаг установлен, то скроллим
                if (shouldScrollToTop || listWasEmpty) {
                    binding.list.scrollToPosition(0)
                    shouldScrollToTop = false // Сбрасываем флаг после скролла
                }
            }
            binding.emptyText.isVisible = state.empty
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error && state.removeErrorPostId != null) {
                Snackbar.make(binding.root, R.string.error_removing_post, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        // При нажатии "Повторить" вызываем новый метод во ViewModel
                        viewModel.retryRemoveById(state.removeErrorPostId)
                    }
                    .show()
                viewModel.resetErrorState()
            }

            if (state.error && state.removeErrorPostId == null && state.likeError) {
                Snackbar.make(binding.root, R.string.error_liking_post, Snackbar.LENGTH_LONG)
                    .show()
                viewModel.resetErrorState()
            }

            if (state.error && state.removeErrorPostId == null && !state.likeError) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
                viewModel.resetErrorState()
            }
            binding.swiperefresh.isRefreshing = state.refreshing
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            println(state)
        }

        // Наблюдаем за количеством новых постов
        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            if (count > 0) {
                // Если есть новые посты, показываем баннер
                binding.newPostsBannerInclude.root.visibility = View.VISIBLE
                binding.newPostsBannerInclude.bannerText.text =
                    getString(R.string.new_posts_available)
            } else {
                // Иначе скрываем баннер
                binding.newPostsBannerInclude.root.visibility = View.GONE
            }
        }

        // Обрабатываем нажатие на баннер, вызывая ViewModel
        binding.newPostsBannerInclude.root.setOnClickListener {
            // Устанавливаем флаг, что нужно скроллить после обновления данных
            shouldScrollToTop = true
            binding.newPostsBannerInclude.root.visibility = View.GONE
            // Запускаем обновление данных во ViewModel
            viewModel.showNewPosts()
        }



        binding.swiperefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        return binding.root
    }
}