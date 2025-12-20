package ru.netology.nmedia.fragments

import android.os.Bundle

import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractorListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.dto.Post
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel


@AndroidEntryPoint
class FeedFragment : Fragment() {

    companion object {
        var Bundle.textArgs by StringArg
    }

    val viewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels() // Инициализация AuthViewModel

    private var shouldScrollToTop =
        false// Флаг для отслеживания необходимости скролла после обновления данных

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

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
                    .add(
                        R.id.fragment_container_for_fullscreen,
                        photoPostFragment
                    ) // Используем .add, чтобы PhotoPostFragment наложился поверх
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

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }
        // 1. ПРАВИЛЬНЫЙ СБОР ДАННЫХ PAGING 3
//        viewLifecycleOwner.lifecycleScope.launch { {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.data.collectLatest {
//                    adapter.submitData(it)
//                }
//            }
//        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                // ПРИСВАИВАЕМ значение свойству isRefreshing
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.append is LoadState.Loading ||
                            state.prepend is LoadState.Loading
            }
        }

        // ОБРАБОТКА ОБНОВЛЕНИЯ ЧЕРЕЗ АДАПТЕР
        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }
        // Обработка ошибок через стейт
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                val message = if (state.removeErrorPostId != null) R.string.error_removing_post else R.string.error_loading
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { adapter.refresh() }
                    .show()
                viewModel.resetErrorState()
            }
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



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFeedBinding.bind(view)

        // *** ДОБАВИТЬ: Подписка на событие запроса аутентификации из PostViewModel ***
        viewModel.authRequiredEvent.observe(viewLifecycleOwner) {
            Log.d("FeedFragment", "Получено событие authRequiredEvent. Показываем диалог.")
            showAuthDialog()
        }

        // Код с Snackbar'ами и loadStateListener, collectLatest и т.д.

        binding.fab.setOnClickListener {
            // *** ДОБАВИТЬ: Проверка аутентификации перед переходом на NewPostFragment ***  // Используем новый метод из AuthViewModel или просто проверяем isAuthorized
            if (authViewModel.isAuthorized) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                showAuthDialog()
            }
        }
    }

    // Метод для отображения диалогового окна
    private fun showAuthDialog() {
        // Используем AlertDialog из androidx.appcompat
        AlertDialog.Builder(requireContext())
            .setTitle("Sign In to NMedia")
            .setMessage("Чтобы выполнить это действие, необходимо войти в аккаунт.")
            .setPositiveButton("Войти") { dialog, which ->
                // При нажатии "Войти", переходим на фрагмент аутентификации
                findNavController().navigate(R.id.signInFragment)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

}