package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() { // Конструктор теперь пустой


    // Реализуем onCreateView вручную
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Раздуваем XML-макет и возвращаем его View
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        return view

        // Также можно использовать return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    // Получаем доступ к AuthViewModel, которая уже есть в AppActivity
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим элементы UI по ID
        val loginEditText = view.findViewById<EditText>(R.id.edit_text_login)
        val passwordEditText = view.findViewById<EditText>(R.id.edit_text_password)
        val signInButton = view.findViewById<Button>(R.id.button_sign_in)

        // Добавляем слушатель нажатий на кнопку
        signInButton.setOnClickListener {
            val login = loginEditText.text.toString()
            val pass = passwordEditText.text.toString()

            if (login.isNotEmpty() && pass.isNotEmpty()) {
                // Вызываем метод входа из ViewModel (который отправляет POST-запрос)
                Toast.makeText(context, "Логин и пароль введены", Toast.LENGTH_SHORT).show()
                authViewModel.signIn(login, pass)
            } else {
                Toast.makeText(context, "Пожалуйста, введите логин и пароль", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        //Наблюдаем за состоянием входа из ViewModel (UIState и Event)

        // Наблюдаем за одноразовым событием успешного входа (из предыдущего ответа)
        authViewModel.signInEvent.observe(viewLifecycleOwner) {
            Toast.makeText(context, "Вход выполнен!", Toast.LENGTH_SHORT)
                .show() // Сообщение об успехе
            // Если вход успешен, возвращаемся назад
            findNavController().popBackStack()
        }

        // Наблюдаем за состоянием загрузки/ошибки
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.uiState.collect { state ->
                when (state) {
                    is AuthViewModel.UiState.Loading -> {
                        signInButton.isEnabled = true
                        // Показать прогресс-бар, если есть
                    }

                    is AuthViewModel.UiState.Error -> {
                        signInButton.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }

                    is AuthViewModel.UiState.Idle -> {
                        signInButton.isEnabled = true
                    }
                }
            }
        }
    }
}