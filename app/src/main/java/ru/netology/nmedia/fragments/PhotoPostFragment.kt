package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nmedia.R

class PhotoPostFragment : Fragment() {

    private lateinit var fullScreenImageView: ImageView

    // Константа для ключа аргумента, по которому будет передаваться URL изображения
    companion object {
        const val ARG_IMAGE_URL = "image_url"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (загружаем XML-разметку)
        val view = inflater.inflate(R.layout.fragment_post_photo, container, false)

        fullScreenImageView = view.findViewById(R.id.fullScreenImageView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем URL изображения из аргументов, переданных этому фрагменту
        val imageUrl = arguments?.getString(ARG_IMAGE_URL)

        imageUrl?.let { url ->
            // загрузка изображение по URL в fullScreenImageView
            Glide.with(this)
                .load(url)
                 .placeholder(R.drawable.ic_loading_100dp) // Опционально: изображение-заглушка
                .into(fullScreenImageView)
        } ?: run {
            // Обработка случая, если URL изображения не был передан или он null
            // Например, можно показать Toast сообщение об ошибке или закрыть фрагмент
             Toast.makeText(requireContext(), "Ошибка: URL изображения не найден", Toast.LENGTH_SHORT).show()
             parentFragmentManager.popBackStack()
        }
    }

    // Статический метод для создания нового экземпляра PhotoPostFragment
    // и передачи ему URL изображения. Так удобнее создавать фрагмент.
    fun newInstance(imageUrl: String): PhotoPostFragment {
        val fragment = PhotoPostFragment()
        val args = Bundle()
        args.putString(ARG_IMAGE_URL, imageUrl)
        fragment.arguments = args
        return fragment
    }
}